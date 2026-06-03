#!/usr/bin/env python3

import sys
import json
import argparse
from pathlib import Path
import psycopg2
from psycopg2.extras import Json
import yaml


def load_config(config_path):
    with open(config_path, 'r', encoding='utf-8') as f:
        config = yaml.safe_load(f)
    required_keys = ['db', 'table', 'tasks_dir']
    for key in required_keys:
        if key not in config:
            raise ValueError(f"В конфиге отсутствует обязательный ключ: {key}")
    return config


def get_db_connection(db_config):
    return psycopg2.connect(**db_config)


def read_file_content(task_name, filename, tasks_dir):
    task_name = task_name.strip()
    filename = filename.strip()
    if not filename:
        return ""
    file_path = Path(tasks_dir) / task_name / filename
    try:
        with open(file_path, "r", encoding="utf-8") as f:
            return f.read().strip()
    except Exception:
        return ""


def normalize_test(test_value, task_name, tasks_dir):
    if isinstance(test_value, dict):
        if "type" in test_value and "referenceQuery" in test_value and "dbScript" in test_value:
            return test_value
        return None

    if isinstance(test_value, str):
        stripped = test_value.strip()
        file_content = read_file_content(task_name, stripped, tasks_dir)
        try:
            return json.loads(file_content)
        except json.JSONDecodeError:
            return file_content
    return None


def migrate_criteria(criteria_dict, task_name, tasks_dir):
    if not criteria_dict or not isinstance(criteria_dict, dict):
        return criteria_dict

    new_criteria = {}
    changed = False

    for test_name, test_data in criteria_dict.items():
        if not isinstance(test_data, dict):
            new_criteria[test_name] = test_data
            continue

        old_test = test_data.get("test")
        new_test = normalize_test(old_test, task_name, tasks_dir)

        if new_test is not None and new_test != old_test:
            updated_data = dict(test_data)
            updated_data["test"] = new_test
            if "special_marker" not in updated_data:
                updated_data["special_marker"] = "NULL"
            new_criteria[test_name] = updated_data
            changed = True
        else:
            if "special_marker" not in test_data:
                updated_data = dict(test_data)
                updated_data["special_marker"] = "NULL"
                new_criteria[test_name] = updated_data
                changed = True
            else:
                new_criteria[test_name] = test_data

    return new_criteria if changed else criteria_dict


def main():
    parser = argparse.ArgumentParser(description="Миграция критериев в БД")
    parser.add_argument(
        '--config', '-c',
        default='config.yaml',
        help='Путь к YAML-файлу конфигурации (по умолчанию config.yaml)'
    )
    args = parser.parse_args()

    try:
        config = load_config(args.config)
    except Exception as e:
        print(f"Ошибка загрузки конфигурации: {e}")
        sys.exit(1)

    db_config = config['db']
    table_config = config['table']
    tasks_dir = config['tasks_dir']

    if not Path(tasks_dir).is_dir():
        print(f"Указанная директория tasks не существует: {tasks_dir}")
        sys.exit(1)

    table_name = table_config['name']
    id_col = table_config['id_column']
    name_col = table_config['name_column']
    criterions_col = table_config['criterions_column']

    conn = get_db_connection(db_config)
    cur = conn.cursor()

    query = f"SELECT {id_col}, {name_col}, {criterions_col} FROM {table_name} WHERE {criterions_col} IS NOT NULL"
    cur.execute(query)
    rows = cur.fetchall()
    print(f"Найдено записей: {len(rows)}")
    updated = 0

    for row_id, task_name, criteria_dict in rows:
        print(f"Обработка: {task_name} (id={row_id})")
        new_dict = migrate_criteria(criteria_dict, task_name, tasks_dir)
        if new_dict != criteria_dict:
            cur.execute(
                f"UPDATE {table_name} SET {criterions_col}=%s WHERE {id_col}=%s",
                (Json(new_dict), row_id)
            )
            updated += 1
            print("     Обновлено")
        else:
            print("     Без изменений")

    conn.commit()
    cur.close()
    conn.close()
    print(f"\nОбновлено записей: {updated}")


if __name__ == "__main__":
    main()

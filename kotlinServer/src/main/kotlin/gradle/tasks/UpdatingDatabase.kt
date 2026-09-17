package gradle.tasks

import checkme.config.AppConfig
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jooq.JSONB
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import java.io.File
import java.sql.DriverManager
import java.sql.SQLException
import java.util.UUID

fun main() {
    val config = AppConfig.fromEnvironment()
    val db = config.databaseConfig
    val tasksPath = "../tasks/"
    val mapper = jacksonObjectMapper()

    try {
        DriverManager.getConnection(db.jdbc, db.user, db.password).use { connection ->
            connection.autoCommit = false
            val dsl = DSL.using(connection, SQLDialect.POSTGRES)
            val results = dsl.fetch("SELECT id, name, criterions FROM tasks")

            for (record in results) {
                val taskId = record.get("id", String::class.java)
                val taskName = record.get("name", String::class.java)
                val criterionsJson = record.get("criterions", String::class.java)
                val taskDirByName = File(tasksPath, taskName)

                val taskDir = when {
                    taskDirByName.isDirectory -> taskDirByName
                    else -> {
                        println("Directory not found for task: $taskName / $taskId")
                        continue
                    }
                }

                val criterions: MutableMap<String, Any> = mapper.readValue(
                    criterionsJson,
                    object : TypeReference<MutableMap<String, Any>>() {}
                )

                var criterionsModified = false
                for ((criterionName, criterionData) in criterions.entries) {
                    if (criterionData is Map<*, *>) {
                        val testField = criterionData["test"]
                        if (testField is String) {
                            val jsonFile = File(taskDir, testField)
                            if (jsonFile.exists()) {
                                val jsonContent = mapper.readTree(jsonFile)
                                val updatedCriterion = criterionData.toMutableMap()
                                updatedCriterion["test"] = jsonContent
                                criterions[criterionName] = updatedCriterion
                                criterionsModified = true
                                jsonFile.delete()
                            } else {
                                println("JSON file not found: ${jsonFile.absolutePath}")
                            }
                        }
                    }
                }

                if (criterionsModified) {
                    val newCriterionsJson = mapper.writeValueAsString(criterions)

                    val criterionsField = DSL.field(DSL.name("criterions"), SQLDataType.JSONB)
                    val idField = DSL.field(DSL.name("id"), SQLDataType.UUID)

                    try {
                        dsl.update(DSL.table(DSL.name("tasks")))
                            .set(criterionsField, JSONB.valueOf(newCriterionsJson))
                            .where(idField.eq(UUID.fromString(taskId)))
                            .execute()
                        connection.commit()
                    } catch (ex: Exception) {
                        println("Update failed: ${ex.javaClass.simpleName}: ${ex.message}")
                    }
                }

                val newTaskDir = File(tasksPath, taskId)
                if (taskDir != newTaskDir) {
                    if (newTaskDir.exists()) {
                        println("Warning: Directory with ID already exists: ${newTaskDir.absolutePath}")
                    } else {
                        val renamed = taskDir.renameTo(newTaskDir)
                        if (!renamed) {
                            println("Failed to rename directory: $taskName -> $taskId")
                        }
                    }
                }
            }
        }
    } catch (e: SQLException) {
        println("Error: ${e.message}")
    }
}
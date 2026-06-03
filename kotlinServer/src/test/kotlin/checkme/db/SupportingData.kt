@file:Suppress("detekt:MayBeConst")

package checkme.db

import checkme.config.AppConfig
import checkme.domain.accounts.PasswordHasher
import checkme.domain.accounts.Role
import checkme.domain.checks.ConsoleCheckTest
import checkme.domain.checks.Criterion
import checkme.domain.checks.SpecialCriteriaMarker
import checkme.domain.forms.CheckResult
import checkme.domain.models.AnswerType
import checkme.domain.models.Bundle
import checkme.domain.models.Check
import checkme.domain.models.Task
import checkme.domain.models.TaskAndOrder
import checkme.web.solution.handlers.COMPLETE_TASK
import java.time.LocalDateTime
import java.util.UUID

val validName = "Иван"

val validSurname = "Иванов"

val validLogin = "iivanov"

val validAdminLogin = "admin"

val validPass = "password"

val validCheckId = listOf(
    UUID.fromString("019b8eb2-139b-7f00-a205-7430d3448420"),
    UUID.fromString("019b8eb2-52c2-71c2-93b2-de4f056b98b7"),
    UUID.fromString("019b8eb2-8868-7144-82be-d2b89c845e06"),
    UUID.fromString("019b8eb2-a5da-73a0-ac83-0a9e737ef5e8"),
    UUID.fromString("019b8eb2-cad9-7471-b3a8-44e2b0e386f3"),
    UUID.fromString("019b8eb2-fb23-7d5a-ab6f-9680e780d29c"),
    UUID.fromString("019b8eb3-176a-739b-8780-747129c3300f"),
    UUID.fromString("019b8eb3-323a-7b3b-ba9c-2c4614538e3e"),
    UUID.fromString("019b8eb3-4d90-7784-8ec5-04c07161dfa7"),
    UUID.fromString("019b8eb3-63e0-70a7-8612-11f7139f7245"),
    UUID.fromString("019b8eb3-922a-723d-9a99-0b3a7f41ae55"),
    UUID.fromString("019be09d-edbd-706b-b0fb-18d2009ec801")
)

val validTaskId = listOf(
    UUID.fromString("019b8eb6-6900-7dcb-8437-7b6c19adde4c"),
    UUID.fromString("019b8eb6-9504-7638-962c-09007467c2e0"),
    UUID.fromString("019b8eb6-ab77-7a12-9daa-207fde9bac44")
)

val validUserId = listOf(
    UUID.fromString("019b8eb7-74ac-7b5a-94ed-8eecc4f79305"),
    UUID.fromString("019b8eb7-9056-74a5-ae02-4bb275b77c7a"),
    UUID.fromString("019b8eb7-a6e4-7c5c-9b97-1f3c7747960c")
)

val validBundleId = listOf(
    UUID.fromString("019b8ebd-30c4-70b6-91f4-4bf848d7dd94"),
    UUID.fromString("019b8ebd-4169-7fe2-a6f8-358f871662ef"),
    UUID.fromString("019b8ebd-53bc-7e76-a14a-6c5f666004aa"),
    UUID.fromString("019b8ebd-652b-7c44-be2a-a273fb29fa3c")
)

val notExistingId = UUID.fromString("00000000-0000-7736-80a2-b2024d9485db")
val notExistingIdForUser = UUID.fromString("00000000-0000-7736-80a2-b2024d9486db")
val notExistingIdForAdmin = UUID.fromString("00000000-0000-7736-80a2-b2024d9487db")

val validRole = Role.STUDENT

val validAdminRole = Role.ADMIN

val validDate = LocalDateTime.parse("2025-05-06T11:48:54.613093")

val config = AppConfig.fromEnvironment()

val validResult = mapOf(
    "check1" to CheckResult(10, "Проверка пройдена"),
    "check2" to CheckResult(0, "Проверка не пройдена")
)

val validResultOne = mapOf(
    "check1" to CheckResult(10, "Проверка пройдена")
)

val validResultMany = mapOf(
    "check1" to CheckResult(10, "Проверка пройдена"),
    "check2" to CheckResult(0, "Проверка не пройдена"),
    "check3" to CheckResult(5, "Проверка пройдена частично"),
    "check4" to CheckResult(3, "Проверка не пройдена не полностью")
)

val validStatusCorrect = "Проверено"

val validStatusProcess = "В процессе"

val validStatusError = "Ошибка выполнения"

val validChecks: List<Check> = listOf(
    Check(validCheckId[0], validTaskId[0], validUserId[0], validDate, validResult, validStatusCorrect, 10),
    Check(validCheckId[1], validTaskId[1], validUserId[0], validDate, validResult, validStatusCorrect, 10),
    Check(validCheckId[2], validTaskId[0], validUserId[1], validDate, emptyMap(), validStatusError),
    Check(validCheckId[3], validTaskId[1], validUserId[0], validDate, emptyMap(), validStatusProcess)
)

val validChecksMany: List<Check> = listOf(
    Check(validCheckId[0], validTaskId[0], validUserId[0], validDate, validResult, validStatusCorrect),
    Check(validCheckId[1], validTaskId[1], validUserId[0], validDate, validResult, validStatusCorrect),
    Check(validCheckId[2], validTaskId[0], validUserId[1], validDate, emptyMap(), validStatusError),
    Check(validCheckId[3], validTaskId[1], validUserId[0], validDate, emptyMap(), validStatusProcess),
    Check(validCheckId[4], validTaskId[0], validUserId[0], validDate, validResult, validStatusCorrect),
    Check(validCheckId[5], validTaskId[1], validUserId[0], validDate, validResult, validStatusCorrect),
    Check(validCheckId[6], validTaskId[0], validUserId[1], validDate, emptyMap(), validStatusError),
    Check(validCheckId[7], validTaskId[0], validUserId[0], validDate, emptyMap(), validStatusProcess),
    Check(validCheckId[8], validTaskId[0], validUserId[0], validDate, emptyMap(), validStatusError),
    Check(validCheckId[9], validTaskId[2], validUserId[0], validDate, validResult, validStatusCorrect),
    Check(validCheckId[10], validTaskId[2], validUserId[0], validDate, validResultOne, validStatusCorrect),
    Check(validCheckId[11], validTaskId[0], validUserId[2], validDate, validResultMany, validStatusCorrect)
)

val validCriterions = mapOf(
    "Сложение положительных чисел" to
        Criterion(
            description = "Сложение чисел происходит корреткно",
            score = COMPLETE_TASK,
            test = ConsoleCheckTest(
                type = "console-check",
                command = "printf \"5\\n3\\n\" | python3 ./*.py",
                expected = "8.0"
            ),
            message = "Сложение происходит некорректно",
            specialMarker = SpecialCriteriaMarker.NULL
        ),
    "Некорректный ввод" to
        Criterion(
            description = "Случай некоректного ввода обрабатывается",
            score = COMPLETE_TASK,
            test = ConsoleCheckTest(
                type = "console-check",
                command = "printf \"a\\n\" | python3 ./*.py",
                expected = "Incorrect input"
            ),
            message = "Не обработан случай некорректного ввода чисел",
            specialMarker = SpecialCriteriaMarker.NULL
        )
)

val validTasks: List<Task> = listOf(
    Task(
        validTaskId[0],
        "Суммирование чисел",
        validCriterions,
        mapOf("Прикрепите файл" to AnswerType.FILE),
        "Вам необходимо написать " +
            "программу, выполняющую суммирование двух чисел. На вход подаются два числа - a и b, " +
            "в качестве результата - сумма этих чисел. Некорректный ввод необходимо обрабатыввать и " +
            "выводить строку \"Incorrect input\" в случае ошибки",
        true
    ),
    Task(
        validTaskId[1],
        "Вычитание чисел",
        validCriterions,
        mapOf("Введите текст" to AnswerType.TEXT),
        "Вам необходимо написать " +
            "программу, выполняющую вычитание двух чисел. На вход подаются два числа - a и b, " +
            "в качестве результата - результат вычитания этих чисел. Некорректный ввод необходимо обрабатывать и " +
            "выводить строку \"Incorrect input\" в случае ошибки",
        false
    ),
    Task(
        validTaskId[2],
        "Тестовое задание",
        validCriterions,
        mapOf(
            "Введите текст" to AnswerType.TEXT,
            "Прикрепите файл" to AnswerType.FILE
        ),
        "Текст тестового задания",
        true
    ),
)

val validPort = 3306
val validHost = "localhost"
val validUserName = "root"
val validPassword = "password"
val validUrlDatabase = "jdbc:mariadb://"
val validOverall = true

val validBundles: List<Bundle> = listOf(
    Bundle(
        validBundleId[0],
        "Контрольная №1",
        true
    ),
    Bundle(
        validBundleId[1],
        "Контрольная №2",
        true
    ),
    Bundle(
        validBundleId[2],
        "Контрольная №3",
        true
    ),
    Bundle(
        validBundleId[3],
        "Контрольная №4",
        true
    ),
)

val validBundleTasks: List<TaskAndOrder> = listOf(
    TaskAndOrder(validTasks[0], 1),
    TaskAndOrder(validTasks[2], 2),
    TaskAndOrder(validTasks[1], 3),
)

val appConfig = AppConfig.fromEnvironment()
val appConfiguredPasswordHasher = PasswordHasher(appConfig.authConfig)

data class TaskWithoutId(
    val name: String,
    val criterions: Map<String, Criterion>,
    val answerFormat: Map<String, AnswerType>,
    val description: String,
    val isActual: Boolean,
)

data class TaskName(
    val name: String,
)

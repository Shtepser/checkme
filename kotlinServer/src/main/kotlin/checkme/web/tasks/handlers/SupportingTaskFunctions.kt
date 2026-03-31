package checkme.web.tasks.handlers

import checkme.domain.checks.ConsoleCheckTest
import checkme.domain.checks.Criterion
import checkme.domain.checks.SqlCheckTest
import checkme.domain.models.AnswerType
import checkme.domain.models.FormatOfAnswer
import checkme.domain.models.Task
import checkme.domain.models.User
import checkme.domain.operations.tasks.CreateTaskError
import checkme.domain.operations.tasks.TaskFetchingError
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.domain.operations.tasks.TaskRemovingError
import checkme.logging.LoggerType
import checkme.logging.ServerLogger
import checkme.web.lenses.TaskLenses
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.http4k.lens.MultipartForm
import org.http4k.lens.MultipartFormField
import org.http4k.lens.MultipartFormFile
import java.io.File
import java.util.UUID

internal fun addTask(
    task: Task,
    taskOperations: TaskOperationsHolder,
): Result<Task, CreationTaskError> {
    return when (
        val newTask = taskOperations.createTask(
            task.name,
            task.criterions,
            task.answerFormat,
            task.description,
            task.isActual
        )
    ) {
        is Success -> Success(newTask.value)
        is Failure -> when (newTask.reason) {
            CreateTaskError.UNKNOWN_DATABASE_ERROR -> Failure(CreationTaskError.UNKNOWN_DATABASE_ERROR)
        }
    }
}

internal fun fetchTask(
    taskId: UUID,
    taskOperations: TaskOperationsHolder,
): Result<Task, FetchingTaskError> {
    return when (
        val fetchedTask = taskOperations.fetchTaskById(taskId)
    ) {
        is Success -> Success(fetchedTask.value)
        is Failure -> when (fetchedTask.reason) {
            TaskFetchingError.NO_SUCH_TASK -> Failure(FetchingTaskError.NO_SUCH_TASK)
            TaskFetchingError.UNKNOWN_DATABASE_ERROR -> Failure(FetchingTaskError.UNKNOWN_DATABASE_ERROR)
        }
    }
}

internal fun fetchTaskWithBestScore(
    taskId: UUID,
    userId: UUID,
    taskOperations: TaskOperationsHolder,
): Result<Task, FetchingTaskError> {
    return when (
        val fetchedTask = taskOperations.fetchTaskByIdWithBestScore(taskId, userId)
    ) {
        is Success -> Success(fetchedTask.value)
        is Failure -> when (fetchedTask.reason) {
            TaskFetchingError.NO_SUCH_TASK -> Failure(FetchingTaskError.NO_SUCH_TASK)
            TaskFetchingError.UNKNOWN_DATABASE_ERROR -> Failure(FetchingTaskError.UNKNOWN_DATABASE_ERROR)
        }
    }
}

internal fun deleteTask(
    task: Task,
    taskOperations: TaskOperationsHolder,
): Result<UUID?, RemovingTaskError> {
    return when (
        val deletedTask = taskOperations.removeTask(task)
    ) {
        is Success -> Success(deletedTask.value)
        is Failure -> when (deletedTask.reason) {
            TaskRemovingError.TASK_NOT_EXISTS -> Failure(RemovingTaskError.NO_SUCH_TASK)
            TaskRemovingError.UNKNOWN_DELETE_ERROR -> Failure(RemovingTaskError.UNKNOWN_DELETE_ERROR)
            TaskRemovingError.UNKNOWN_DATABASE_ERROR -> Failure(RemovingTaskError.UNKNOWN_DATABASE_ERROR)
        }
    }
}

internal fun fetchAllTasks(taskOperations: TaskOperationsHolder): Result<List<Task>, FetchingTaskError> {
    return when (
        val fetchedTasks = taskOperations.fetchAllTasks()
    ) {
        is Success -> Success(fetchedTasks.value)
        is Failure -> when (fetchedTasks.reason) {
            TaskFetchingError.NO_SUCH_TASK -> Failure(FetchingTaskError.NO_SUCH_TASK)
            TaskFetchingError.UNKNOWN_DATABASE_ERROR -> Failure(FetchingTaskError.UNKNOWN_DATABASE_ERROR)
        }
    }
}

internal fun fetchHiddenTasks(taskOperations: TaskOperationsHolder): Result<List<Task>, FetchingTaskError> {
    return when (
        val fetchedTasks = taskOperations.fetchHiddenTasks()
    ) {
        is Success -> Success(fetchedTasks.value)
        is Failure -> when (fetchedTasks.reason) {
            TaskFetchingError.NO_SUCH_TASK -> Failure(FetchingTaskError.NO_SUCH_TASK)
            TaskFetchingError.UNKNOWN_DATABASE_ERROR -> Failure(FetchingTaskError.UNKNOWN_DATABASE_ERROR)
        }
    }
}

internal fun taskExists(
    taskId: UUID,
    taskOperations: TaskOperationsHolder,
): Boolean {
    return when (fetchTask(taskId = taskId, taskOperations = taskOperations)) {
        is Success -> true
        is Failure -> false
    }
}

@Suppress("ReturnCount")
fun MultipartForm.validateForm(): Result<Task, ValidateTaskError> {
    val jacksonMapper = jacksonObjectMapper()
    val taskName = TaskLenses.nameField(this).value
    val description = TaskLenses.descriptionField(this).value
    val criterions: Map<String, Criterion> = try {
        jacksonMapper.readValue<Map<String, Criterion>>(TaskLenses.criterionsField(this).value)
    } catch (_: Exception) {
        return Failure(ValidateTaskError.INVALID_CHECK_TYPE)
    }
    val answerFormatFromForm: List<FormatOfAnswer> =
        jacksonMapper.readValue<List<FormatOfAnswer>>(TaskLenses.answerFormatField(this).value)

    for (answerFormat in answerFormatFromForm) {
        try {
            AnswerType.valueOf(answerFormat.type.uppercase())
        } catch (_: IllegalArgumentException) {
            return Failure(ValidateTaskError.ANSWER_TYPE_ERROR)
        }
    }
    val answerFormatBd = answerFormatFromForm.associate { it.name to AnswerType.valueOf(it.type.uppercase()) }
//    val files = TaskLenses.filesField(this)
    //todo
//    for (criterion in criterions) {
//        if (!files.map { it.filename }.contains(criterion.value.test)) {
//            return Failure(ValidateTaskError.NO_SUCH_FILE_FOR_CRITERION)
//        }
//    }

    return Success(
        Task(
            id = UUID.fromString("00000000-0000-7000-8000-000000000000"),
            name = taskName,
            criterions = criterions,
            answerFormat = answerFormatBd,
            description = description,
            isActual = true
        )
    )
}

// первоначально функция добавляет все файлы с проверками, относящиеся к заданию, в соответствующую директорию,
// затем вызывается функция tryRenameFileAndUpdateCriterions для обновления имен файлов-проверок с особыми критериями
fun Task.addTaskFilesToDirectory(
//    user: User,
    files: Map<String, List<MultipartFormFile>>,
//    fields: Map<String, List<MultipartFormField>>,
    criterions: Map<String, Criterion>,
//    overall: Boolean,
): Map<String, Criterion> {
    val tasksDir = File(
        "..$TASKS_DIR" +
                "/${this.name.trim()}"
    )
    if (!tasksDir.exists()) {
        tasksDir.mkdirs()
    }
    for (file in files.values.flatten()) {
        val filePath = File(tasksDir, file.filename)
        val fileBytes = file.content.use { it.readAllBytes() }
        filePath.writeBytes(fileBytes)
    }
    return criterions
}

//todo
// Функция возвращает обновленный список критериев.
// Если у задания есть особые проверки из списка specialCriteria, то файл такой проверки получает новое
// название - "beforeAll", "beforeEach", "afterEach", "afterAll" в соответствие со своим типом.
// Если имя было изменено, фиксируем это в критериях задания для последующего исполнения.
//@Suppress("NestedBlockDepth")
//fun tryRenameFileAndUpdateCriterions(
//    user: User,
//    criterions: Map<String, Criterion>,
//    fields: Map<String, List<MultipartFormField>>,
//    tasksDir: File,
//    overall: Boolean,
//): Map<String, Criterion> {
//    val updatedCriterions = criterions.toMutableMap()
//    val specialCriteria = listOf("beforeAll", "beforeEach", "afterEach", "afterAll")
//    for (criteria in specialCriteria) {
//        fields[criteria]?.firstOrNull()?.value?.takeIf { it.isNotBlank() }?.let { originalFileName ->
//            val originalFile = File(tasksDir, originalFileName)
//
//            if (originalFile.exists()) {
//                val newFile = File(tasksDir, "$criteria.json")
//                originalFile.renameTo(newFile)
//                if (overall) {
//                    ServerLogger.log(
//                        user = user,
//                        action = "Working with task files",
//                        message = "Renamed $originalFileName to ${newFile.name} for criteria $criteria",
//                        type = LoggerType.INFO
//                    )
//                }
//
//                updatedCriterions.forEach { (key, value) ->
//                    if (value.test == originalFileName) {
//                        updatedCriterions[key] = value.copy(test = "$criteria.json")
//                    }
//                }
//            } else {
//                ServerLogger.log(
//                    user = user,
//                    action = "Add task warnings",
//                    message = "Warning: file $originalFileName not found for criteria $criteria",
//                    type = LoggerType.WARN
//                )
//            }
//        }
//    }
//    return updatedCriterions
//}

enum class CreationTaskError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
}

enum class ValidateTaskError(val errorText: String) {
    INVALID_CHECK_TYPE("All specified files must be added"),
    ANSWER_TYPE_ERROR("This type of task answer does not exist"),
    USER_HAS_NOT_RIGHTS("Not allowed to add task"),
}

enum class FetchingTaskError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
    NO_SUCH_TASK("The task does not exist"),
}

enum class RemovingTaskError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
    NO_SUCH_TASK("The task does not exist"),
    UNKNOWN_DELETE_ERROR("Something was wrong until task deleting. Please try again later."),
}

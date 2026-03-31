package checkme.web.tasks.handlers

import checkme.config.LoggingConfig
import checkme.domain.models.Task
import checkme.domain.models.User
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.logging.LoggerType
import checkme.logging.ServerLogger
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.TaskLenses
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import org.http4k.lens.MultipartForm
import org.http4k.lens.RequestContextLens

const val TASKS_DIR = "/tasks"

class AddTaskHandler(
    private val tasksOperations: TaskOperationsHolder,
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val form: MultipartForm = TaskLenses.multipartFormFieldsAll(request)
        return when {
            user == null || !user.isAdmin() ->
                objectMapper.sendBadRequestError(ValidateTaskError.USER_HAS_NOT_RIGHTS.errorText)

            else -> {
                when (
                    val validatedNewTask = form.validateForm()
                ) {
                    is Failure -> {
                        ServerLogger.log(
                            user = user,
                            action = "New task addition warnings",
                            message = "Something wrong with task data. Error: ${validatedNewTask.reason.errorText}",
                            type = LoggerType.WARN
                        )
                        objectMapper.sendBadRequestError(validatedNewTask.reason.errorText)
                    }

                    is Success -> {
                        tryAddTaskAndFiles(
                            user = user,
                            validatedNewTask = validatedNewTask.value,
                            taskOperations = tasksOperations,
                            objectMapper = objectMapper,
                            form = form
                        )
                    }
                }
            }
        }
    }
}

@Suppress("LongParameterList")
private fun tryAddTaskAndFiles(
    user: User,
    validatedNewTask: Task,
    taskOperations: TaskOperationsHolder,
    objectMapper: ObjectMapper,
//    overall: Boolean,
    form: MultipartForm,
): Response {
    val criterions = validatedNewTask.addTaskFilesToDirectory(
//        user = user,
        files = form.files,
//        fields = form.fields,
        criterions = validatedNewTask.criterions,
//        overall = overall
    )
    return when (
        val newTask =
            addTask(
                task = validatedNewTask.copy(criterions = criterions),
                taskOperations = taskOperations
            )
    ) {
        is Success -> {
            ServerLogger.log(
                user = user,
                action = "New task addition",
                message = "User is created new task ${newTask.value.id}-${newTask.value.name}",
                type = LoggerType.INFO
            )
            objectMapper.sendOKResponse(mapOf("taskId" to newTask.value.id))
        }

        is Failure -> {
            ServerLogger.log(
                user = user,
                action = "New task addition warnings",
                message = "Something wrong when try add task. Error: ${newTask.reason.errorText}",
                type = LoggerType.WARN
            )
            objectMapper.sendBadRequestError(newTask.reason.errorText)
        }
    }
}

package checkme.web.tasks.handlers

import checkme.domain.models.Task
import checkme.domain.models.User
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.logging.LoggerType
import checkme.logging.ServerLogger
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.GeneralWebLenses.idOrNull
import checkme.web.lenses.TaskLenses
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.lens.MultipartForm
import org.http4k.lens.RequestContextLens

class ChangeTaskHandler(
    private val tasksOperations: TaskOperationsHolder,
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val taskId = request.idOrNull()
            ?: return objectMapper.sendBadRequestError(ChangeTaskError.NO_ID_TO_CHANGE_TASK.errorText)
        val form: MultipartForm = TaskLenses.multipartFormFieldsAll(request)
        return when {
            user?.isAdmin() != true ->
                objectMapper.sendBadRequestError(ChangeTaskError.USER_HAS_NOT_RIGHTS.errorText)

            else -> {
                when (
                    val validatedTask = form.validateForm()
                ) {
                    is Failure -> {
                        ServerLogger.log(
                            user = user,
                            action = "Task editing warnings",
                            message = "Something wrong with task data. Error: ${validatedTask.reason.errorText}",
                            type = LoggerType.WARN
                        )
                        objectMapper.sendBadRequestError(validatedTask.reason.errorText)
                    }

                    is Success -> when (
                        val taskToChange = fetchTask(
                            taskId = taskId,
                            taskOperations = tasksOperations
                        )
                    ) {
                        is Failure -> objectMapper.sendBadRequestError(ChangeTaskError.TASK_NOT_EXISTS.errorText)
                        is Success -> tryChangeTask(
                            user = user,
                            taskToChange = taskToChange.value,
                            validatedTask = validatedTask.value,
                            objectMapper = objectMapper,
                            tasksOperations = tasksOperations,
                            form = form
                        )
                    }
                }
            }
        }
    }
}

private fun tryChangeTask(
    user: User,
    taskToChange: Task,
    validatedTask: Task,
    objectMapper: ObjectMapper,
    tasksOperations: TaskOperationsHolder,
    form: MultipartForm,
): Response {
    return when (
        val editedTask = changeTask(taskToChange, tasksOperations)
    ) {
        is Success -> {
            val isReplaced = validatedTask.replaceTaskFilesInDirectory(
                nameDirectory = taskToChange.id.toString(),
                files = form.files,
                criterions = validatedTask.criterions,
            )
            when (isReplaced) {
                is Success -> {
                    ServerLogger.log(
                        user = user,
                        action = "Task edited",
                        message = "User is changed task ${editedTask.value.id}-${editedTask.value.name}",
                        type = LoggerType.INFO
                    )
                    objectMapper.sendOKResponse(mapOf("taskId" to editedTask.value.id))
                }
                is Failure -> {
                    ServerLogger.log(
                        user = user,
                        action = "Task edited warnings",
                        message = "Something wrong when try edit Task. Error: ${isReplaced.reason}",
                        type = LoggerType.WARN
                    )
                    objectMapper.sendBadRequestError(isReplaced.reason)
                }
            }
        }

        is Failure -> {
            ServerLogger.log(
                user = user,
                action = "Task edited warnings",
                message = "Something wrong when try edit Task. Error: ${editedTask.reason.errorText}",
                type = LoggerType.WARN
            )
            objectMapper.sendBadRequestError(editedTask.reason.errorText)
        }
    }
}

enum class ChangeTaskError(val errorText: String) {
    NO_ID_TO_CHANGE_TASK("No id to change task"),
    TASK_NOT_EXISTS("Task with this id doesnt exists"),
    USER_HAS_NOT_RIGHTS("Not allowed to change task"),
}

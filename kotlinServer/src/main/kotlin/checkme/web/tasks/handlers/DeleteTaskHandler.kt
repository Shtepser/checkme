package checkme.web.tasks.handlers

import checkme.domain.models.Task
import checkme.domain.models.User
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.logging.LoggerType
import checkme.logging.ServerLogger
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.GeneralWebLenses.idOrNull
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import org.http4k.lens.RequestContextLens

class DeleteTaskHandler(
    private val tasksOperations: TaskOperationsHolder,
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val taskId = request.idOrNull()
            ?: return objectMapper.sendBadRequestError(DeleteTaskError.NO_ID_TO_DELETE_TASK.errorText)
        return when {
            user?.isAdmin() == true ->
                when (
                    val taskToDelete = fetchTask(
                        taskId = taskId,
                        taskOperations = tasksOperations
                    )
                ) {
                    is Failure -> objectMapper.sendBadRequestError(DeleteTaskError.TASK_NOT_EXISTS.errorText)
                    is Success -> tryDeleteTask(
                        user = user,
                        taskToDelete = taskToDelete.value,
                        objectMapper = objectMapper,
                        tasksOperations = tasksOperations
                    )
                }

            else -> objectMapper.sendBadRequestError(DeleteTaskError.USER_HAS_NOT_RIGHTS.errorText)
        }
    }
}

private fun tryDeleteTask(
    user: User,
    taskToDelete: Task,
    objectMapper: ObjectMapper,
    tasksOperations: TaskOperationsHolder,
): Response {
    return when (val deleteDirectoryFlag = taskToDelete.deleteTaskDirectory(taskToDelete.id.toString())) {
        is Failure -> objectMapper.sendBadRequestError(deleteDirectoryFlag.reason)

        is Success -> when (
            val deleteFlag = deleteTask(
                task = taskToDelete,
                taskOperations = tasksOperations
            )
        ) {
            is Failure -> objectMapper.sendBadRequestError(deleteFlag.reason.errorText)

            is Success -> {
                ServerLogger.log(
                    user = user,
                    action = "Task deletion",
                    message = "User delete task ${taskToDelete.id}-${taskToDelete.name}",
                    type = LoggerType.INFO
                )
                objectMapper.sendOKResponse(mapOf("status" to "complete"))
            }
        }
    }
}

enum class DeleteTaskError(val errorText: String) {
    NO_ID_TO_DELETE_TASK("No id to delete task"),
    TASK_NOT_EXISTS("Task with this id doesnt exists"),
    USER_HAS_NOT_RIGHTS("Not allowed to delete task"),
}

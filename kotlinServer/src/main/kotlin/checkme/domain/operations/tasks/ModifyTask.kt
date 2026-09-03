package checkme.domain.operations.tasks

import checkme.domain.checks.Criterion
import checkme.domain.models.AnswerType
import checkme.domain.models.Task
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.jooq.exception.DataAccessException
import java.util.UUID

class RemoveTask(
    private val selectTaskById: (taskId: UUID) -> Task?,
    private val removeTask: (UUID) -> Int?,
) : (Task) -> Result<UUID?, TaskRemovingError> {
    override fun invoke(task: Task): Result<UUID?, TaskRemovingError> {
        return try {
            when {
                taskNotExists(task.id) -> Failure(TaskRemovingError.TASK_NOT_EXISTS)
                else -> when (removeTask(task.id)) {
                    is Int -> Success(task.id)
                    else -> Failure(TaskRemovingError.UNKNOWN_DELETE_ERROR)
                }
            }
        } catch (_: DataAccessException) {
            Failure(TaskRemovingError.UNKNOWN_DATABASE_ERROR)
        }
    }

    private fun taskNotExists(taskId: UUID): Boolean =
        when (selectTaskById(taskId)) {
            is Task -> false
            else -> true
        }
}

class ModifyTaskActuality(
    private val updateTaskActuality: (
        task: Task,
    ) -> Task?,
) : (Task) -> Result<Task, TaskModifyActualityError> {
    override operator fun invoke(task: Task): Result<Task, TaskModifyActualityError> =
        when (
            val editedTask = updateTaskActuality(
                task
            )
        ) {
            is Task -> Success(editedTask)
            else -> {
                Failure(TaskModifyActualityError.UNKNOWN_DATABASE_ERROR)
            }
        }
}

class ModifyTask(
    private val selectTaskById: (taskId: UUID) -> Task?,
    private val updateTask: (
        taskId: UUID,
        name: String,
        criterions: Map<String, Criterion>,
        answerFormat: Map<String, AnswerType>,
        description: String,
    ) -> Task?,
) : (UUID, String, Map<String, Criterion>, Map<String, AnswerType>, String) -> Result<Task, TaskModifyError> {
    override fun invoke(
        taskId: UUID,
        name: String,
        criterions: Map<String, Criterion>,
        answerFormat: Map<String, AnswerType>,
        description: String,
    ): Result<Task, TaskModifyError> {
        return try {
            when {
                when (selectTaskById(taskId)) {
                    is Task -> false
                    else -> true
                } -> Failure(TaskModifyError.TASK_NOT_EXISTS)
                else -> when (
                    val editedTask = updateTask(
                        taskId,
                        name,
                        criterions,
                        answerFormat,
                        description
                    )
                ) {
                    is Task -> Success(editedTask)
                    else -> Failure(TaskModifyError.UNKNOWN_UPDATE_ERROR)
                }
            }
        } catch (_: DataAccessException) {
            Failure(TaskModifyError.UNKNOWN_DATABASE_ERROR)
        }
    }
}

enum class TaskRemovingError {
    UNKNOWN_DATABASE_ERROR,
    UNKNOWN_DELETE_ERROR,
    TASK_NOT_EXISTS,
}

enum class TaskModifyActualityError {
    UNKNOWN_DATABASE_ERROR,
}

enum class TaskModifyError {
    UNKNOWN_DATABASE_ERROR,
    UNKNOWN_UPDATE_ERROR,
    TASK_NOT_EXISTS,
}

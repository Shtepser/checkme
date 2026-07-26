package checkme.domain.operations.dependencies.tasks

import checkme.domain.checks.Criterion
import checkme.domain.models.AnswerType
import checkme.domain.models.Task
import checkme.web.solution.forms.TaskIdAndName
import checkme.web.solution.forms.TaskNameForAllResults
import java.util.UUID

interface TasksDatabase {
    fun selectTaskById(taskId: UUID): Task?

    fun selectTaskByIdWithBestScore(
        taskId: UUID,
        userId: UUID,
    ): Task?

    fun selectAllTask(): List<Task>

    fun selectHiddenTasks(): List<Task>

    fun selectAllTasksIdAndNames(): List<TaskIdAndName>

    fun selectAllTasksPagination(page: Int): List<Task>

    fun selectTaskName(taskId: UUID): TaskNameForAllResults?

    fun updateTaskActuality(task: Task): Task?

    fun insertTask(
        name: String,
        criterions: Map<String, Criterion>,
        answerFormat: Map<String, AnswerType>,
        description: String,
        isActual: Boolean,
    ): Task?

    fun deleteTask(taskId: UUID): Int

    fun updateTask(
        taskId: UUID,
        name: String,
        criterions: Map<String, Criterion>,
        answerFormat: Map<String, AnswerType>,
        description: String,
    ): Task?
}

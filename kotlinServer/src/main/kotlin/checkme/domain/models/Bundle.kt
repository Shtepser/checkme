package checkme.domain.models

import java.util.UUID

data class Bundle(
    val id: UUID,
    val name: String,
    val isActual: Boolean,
)

data class TaskAndOrder(
    val task: Task,
    val order: Int,
)

data class BundleAndTasks(
    val bundle: Bundle,
    val tasks: List<TaskAndOrder>,
)

data class BundleTasksWithBestResult(
    val bundleName: String,
    val taskId: UUID,
    val taskName: String,
    val highestScore: Int,
    val bestSolution: Int,
)

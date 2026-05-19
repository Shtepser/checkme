package checkme.web.tasks

import checkme.domain.operations.OperationHolder
import checkme.web.context.ContextTools
import checkme.web.tasks.handlers.AddTaskHandler
import checkme.web.tasks.handlers.ChangeTaskActualityHandler
import checkme.web.tasks.handlers.DeleteTaskHandler
import checkme.web.tasks.handlers.TaskHandler
import checkme.web.tasks.handlers.TasksHiddenListHandler
import checkme.web.tasks.handlers.TasksListHandler
import org.http4k.core.*
import org.http4k.routing.*

fun taskRouter(
    operations: OperationHolder,
    contextTools: ContextTools,
): RoutingHttpHandler =
    routes(
        NEW_TASK bind Method.POST to AddTaskHandler(
            tasksOperations = operations.taskOperations,
            userLens = contextTools.userLens
        ),
        "$DELETE_TASK/{id}" bind Method.DELETE to DeleteTaskHandler(
            tasksOperations = operations.taskOperations,
            userLens = contextTools.userLens
        ),
        "$CHANGE_ACTUALITY/{id}" bind Method.POST to ChangeTaskActualityHandler(
            tasksOperations = operations.taskOperations,
            userLens = contextTools.userLens
        ),
        "/all" bind Method.GET to TasksListHandler(
            taskOperations = operations.taskOperations,
            bundleOperations = operations.bundleOperations,
            userLens = contextTools.userLens
        ),
        "/hidden" bind Method.GET to TasksHiddenListHandler(
            taskOperations = operations.taskOperations,
            bundleOperations = operations.bundleOperations,
            userLens = contextTools.userLens
        ),
        "/{id}" bind Method.GET to TaskHandler(
            userLens = contextTools.userLens,
            taskOperations = operations.taskOperations
        )
    )

const val TASK_SEGMENT = "/task"
const val NEW_TASK = "/new"
const val DELETE_TASK = "/delete"
const val CHANGE_ACTUALITY = "/change-actuality"

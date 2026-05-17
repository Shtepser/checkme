package checkme.web.solution.handlers

import checkme.domain.models.User
import checkme.domain.operations.bundles.BundleOperationHolder
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.web.bundles.handlers.selectBundleTasksWithUserBestResult
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.GeneralWebLenses.pageCountOrNull
import checkme.web.solution.forms.BundleWithTaskAndBestResult
import checkme.web.solution.forms.TaskWithBestResult
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import org.http4k.lens.RequestContextLens
import java.util.UUID

class ListMyResultHandler(
    private val taskOperations: TaskOperationsHolder,
    private val userLens: RequestContextLens<User?>,
    private val bundleOperations: BundleOperationHolder,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val page = request.pageCountOrNull() ?: 1
        return when {
            user == null -> objectMapper.sendBadRequestError(ViewCheckResultError.USER_HAS_NOT_RIGHTS)
            else -> tryFetchBundleTasksWithUserBestResult(
                page = page,
                userId = user.id,
                objectMapper = objectMapper,
                taskOperations = taskOperations,
                bundleOperation = bundleOperations
            )
        }
    }
}

private fun tryFetchBundleTasksWithUserBestResult(
    page: Int,
    userId: UUID,
    objectMapper: ObjectMapper,
    taskOperations: TaskOperationsHolder,
    bundleOperation: BundleOperationHolder,
): Response {
    return when (
        val bundleTasks = selectBundleTasksWithUserBestResult(
            page,
            userId,
            bundleOperation
        )
    ) {
        is Failure -> objectMapper.sendBadRequestError(bundleTasks.reason.errorText)

        is Success -> {
            val bundleList = mutableListOf<BundleWithTaskAndBestResult>()
            for (task in bundleTasks.value.distinctBy { it.bundleName }) {
                bundleList.add(
                    BundleWithTaskAndBestResult(
                        bundleName = task.bundleName,
                        taskWithBestResult = mutableListOf(),
                    )
                )
            }
            val bundleByName = bundleList.associateBy { it.bundleName }
            bundleTasks.value.forEach { task ->
                val taskData = taskOperations.fetchTaskName(task.taskId)
                if (taskData is Failure) objectMapper.sendBadRequestError()
                bundleByName[task.bundleName]?.taskWithBestResult?.add(
                    TaskWithBestResult(
                        taskName = task.taskName,
                        taskId = task.taskId.toString(),
                        highestScore = task.highestScore,
                        bestSolution = task.bestSolution,
                    )
                )
            }
            objectMapper.sendOKResponse(bundleList)
        }
    }
}

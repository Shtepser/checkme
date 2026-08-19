package checkme.web.bundles.handlers

import checkme.domain.models.Bundle
import checkme.domain.models.BundleAndTasks
import checkme.domain.models.User
import checkme.domain.operations.bundles.BundleOperationHolder
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.GeneralWebLenses.idOrNull
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import org.http4k.lens.RequestContextLens
import java.util.UUID

class BundleHandler(
    private val userLens: RequestContextLens<User?>,
    private val bundleOperations: BundleOperationHolder,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val bundleId = request.idOrNull()
        return when {
            user == null -> objectMapper.sendBadRequestError(ViewBundleError.USER_CANT_VIEW_THIS_BUNDLE.errorText)
            bundleId == null -> objectMapper.sendBadRequestError(ViewBundleError.NO_BUNDLE_ID_ERROR.errorText)
            else -> tryFetchBundleAndTasks(
                bundleId = bundleId,

                objectMapper = objectMapper,
                bundleOperations = bundleOperations,
                user = user
            )
        }
    }
}

private fun tryFetchBundleAndTasks(
    bundleId: UUID,
    objectMapper: ObjectMapper,
    bundleOperations: BundleOperationHolder,
    user: User,
): Response {
    return when (val bundle = selectBundle(bundleId, bundleOperations)) {
        is Failure -> return objectMapper.sendBadRequestError(bundle.reason.errorText)
        is Success -> {
            if (!bundle.value.isActual && !user.isAdmin()) {
                objectMapper.sendBadRequestError(ViewBundleError.USER_CANT_VIEW_THIS_BUNDLE.errorText)
            } else {
                tryFetchBundleTasks(
                    bundle = bundle.value,
                    user = user,
                    bundleOperations = bundleOperations,
                    objectMapper = objectMapper
                )
            }
        }
    }
}

private fun tryFetchBundleTasks(
    bundle: Bundle,
    user: User,
    bundleOperations: BundleOperationHolder,
    objectMapper: ObjectMapper,
): Response {
    return when (val bundleTasks = selectBundleTasksWithBestScore(bundle.id, user.id, bundleOperations)) {
        is Failure -> objectMapper.sendBadRequestError(bundleTasks.reason.errorText)
        is Success -> objectMapper.sendOKResponse(BundleAndTasks(bundle = bundle, tasks = bundleTasks.value))
    }
}

enum class ViewBundleError(val errorText: String) {
    NO_BUNDLE_ID_ERROR("No bundle id to view bundle info"),
    USER_CANT_VIEW_THIS_BUNDLE("User can't view this bundle"),
}

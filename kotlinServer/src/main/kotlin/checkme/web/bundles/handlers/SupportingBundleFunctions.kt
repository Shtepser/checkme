@file:Suppress("TooManyFunctions")

package checkme.web.bundles.handlers

import checkme.domain.models.Bundle
import checkme.domain.models.BundleTasksWithBestResult
import checkme.domain.models.TaskAndOrder
import checkme.domain.operations.bundles.BundleFetchingError
import checkme.domain.operations.bundles.BundleOperationHolder
import checkme.domain.operations.bundles.BundleRemovingError
import checkme.domain.operations.bundles.CreateBundleError
import checkme.domain.operations.bundles.ModifyBundleError
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import java.util.UUID

internal fun addBundle(
    bundleName: String,
    bundleOperations: BundleOperationHolder,
): Result<Bundle, CreationBundleError> {
    return when (
        val newBundle = bundleOperations.createBundle(bundleName)
    ) {
        is Success -> Success(newBundle.value)
        is Failure -> when (newBundle.reason) {
            CreateBundleError.UNKNOWN_DATABASE_ERROR -> Failure(CreationBundleError.UNKNOWN_DATABASE_ERROR)
        }
    }
}

internal fun selectBundle(
    bundleId: UUID,
    bundleOperations: BundleOperationHolder,
): Result<Bundle, FetchingBundleError> {
    return when (
        val bundle = bundleOperations.fetchBundleById(bundleId)
    ) {
        is Success -> Success(bundle.value)
        is Failure -> when (bundle.reason) {
            BundleFetchingError.UNKNOWN_DATABASE_ERROR -> Failure(FetchingBundleError.UNKNOWN_DATABASE_ERROR)
            BundleFetchingError.NO_SUCH_BUNDLE -> Failure(FetchingBundleError.NO_SUCH_BUNDLE)
        }
    }
}

internal fun selectBundleTasks(
    bundleId: UUID,
    bundleOperations: BundleOperationHolder,
): Result<List<TaskAndOrder>, FetchingBundleTasksError> {
    return when (
        val tasks = bundleOperations.fetchBundleTasksById(bundleId)
    ) {
        is Success -> Success(tasks.value)
        is Failure -> when (tasks.reason) {
            BundleFetchingError.NO_SUCH_BUNDLE -> Failure(FetchingBundleTasksError.NO_SUCH_BUNDLE)
            BundleFetchingError.UNKNOWN_DATABASE_ERROR -> Failure(FetchingBundleTasksError.UNKNOWN_DATABASE_ERROR)
        }
    }
}

internal fun selectBundles(bundleOperations: BundleOperationHolder): Result<List<Bundle>, FetchingBundleError> {
    return when (
        val bundles = bundleOperations.fetchAllBundles()
    ) {
        is Success -> Success(bundles.value)
        is Failure -> when (bundles.reason) {
            BundleFetchingError.UNKNOWN_DATABASE_ERROR -> Failure(FetchingBundleError.UNKNOWN_DATABASE_ERROR)
            BundleFetchingError.NO_SUCH_BUNDLE -> Failure(FetchingBundleError.NO_SUCH_BUNDLE)
        }
    }
}

internal fun selectTaskBundles(
    taskId: UUID,
    bundleOperations: BundleOperationHolder,
): Result<List<Bundle>, FetchingBundleError> {
    return when (
        val fetchedBundles = bundleOperations.fetchBundlesByTaskId(taskId)
    ) {
        is Success -> Success(fetchedBundles.value)
        is Failure -> when (fetchedBundles.reason) {
            BundleFetchingError.NO_SUCH_BUNDLE -> Failure(FetchingBundleError.NO_SUCH_BUNDLE)
            BundleFetchingError.UNKNOWN_DATABASE_ERROR -> Failure(FetchingBundleError.UNKNOWN_DATABASE_ERROR)
        }
    }
}

internal fun selectBundleTasksWithUserBestResult(
    page: Int,
    userId: UUID,
    bundleOperations: BundleOperationHolder,
): Result<List<BundleTasksWithBestResult>, FetchingBundleError> {
    return when (
        val bundleTasks = bundleOperations.fetchBundleTasksWithUserBestResult(page, userId)
    ) {
        is Success -> Success(bundleTasks.value)
        is Failure -> when (bundleTasks.reason) {
            BundleFetchingError.NO_SUCH_BUNDLE -> Failure(FetchingBundleError.NO_SUCH_BUNDLE)
            BundleFetchingError.UNKNOWN_DATABASE_ERROR -> Failure(FetchingBundleError.UNKNOWN_DATABASE_ERROR)
        }
    }
}

internal fun selectHiddenBundles(bundleOperations: BundleOperationHolder): Result<List<Bundle>, FetchingBundleError> {
    return when (
        val bundles = bundleOperations.fetchHiddenBundles()
    ) {
        is Success -> Success(bundles.value)
        is Failure -> when (bundles.reason) {
            BundleFetchingError.UNKNOWN_DATABASE_ERROR -> Failure(FetchingBundleError.UNKNOWN_DATABASE_ERROR)
            BundleFetchingError.NO_SUCH_BUNDLE -> Failure(FetchingBundleError.NO_SUCH_BUNDLE)
        }
    }
}

internal fun tryUpdateBundleTasks(
    tasksAndOrder: List<TaskAndOrder>,
    bundleId: UUID,
    bundleOperations: BundleOperationHolder,
): Result<List<TaskAndOrder>, CreationBundleTasksError> {
    return when (val validatedTasks = validateBundleTasks(tasksAndOrder)) {
        is Failure -> Failure(validatedTasks.reason)
        is Success -> {
            when (val updatedTasks = bundleOperations.modifyBundleTasks(bundleId, tasksAndOrder)) {
                is Success -> Success(updatedTasks.value)
                is Failure -> when (updatedTasks.reason) {
                    ModifyBundleError.NO_SUCH_BUNDLE -> Failure(CreationBundleTasksError.NO_SUCH_BUNDLE_FOR_TASKS)
                    ModifyBundleError.UNKNOWN_DATABASE_ERROR -> Failure(CreationBundleTasksError.UNKNOWN_DATABASE_ERROR)
                }
            }
        }
    }
}

internal fun changeBundleActuality(
    bundle: Bundle,
    bundleOperations: BundleOperationHolder,
): Result<Bundle, BundleChangingError> {
    return when (
        val updatedBundle = bundleOperations.modifyBundleActuality(bundle)
    ) {
        is Success -> Success(updatedBundle.value)
        is Failure -> when (updatedBundle.reason) {
            ModifyBundleError.NO_SUCH_BUNDLE -> Failure(BundleChangingError.NO_SUCH_BUNDLE)
            ModifyBundleError.UNKNOWN_DATABASE_ERROR -> Failure(BundleChangingError.UNKNOWN_DATABASE_ERROR)
        }
    }
}

internal fun changeBundleName(
    bundle: Bundle,
    bundleOperations: BundleOperationHolder,
): Result<Bundle, BundleChangingError> {
    return when (
        val updatedBundle = bundleOperations.modifyBundle(bundle)
    ) {
        is Success -> Success(updatedBundle.value)
        is Failure -> when (updatedBundle.reason) {
            ModifyBundleError.NO_SUCH_BUNDLE -> Failure(BundleChangingError.NO_SUCH_BUNDLE)
            ModifyBundleError.UNKNOWN_DATABASE_ERROR -> Failure(BundleChangingError.UNKNOWN_DATABASE_ERROR)
        }
    }
}

internal fun deleteBundle(
    bundle: Bundle,
    bundleOperations: BundleOperationHolder,
): Result<Boolean, RemovingBundleError> {
    return when (
        val deletedBundle = bundleOperations.removeBundle(bundle)
    ) {
        is Success -> Success(deletedBundle.value)
        is Failure -> when (deletedBundle.reason) {
            BundleRemovingError.BUNDLE_NOT_EXISTS -> Failure(RemovingBundleError.NO_SUCH_BUNDLE)
            BundleRemovingError.UNKNOWN_DATABASE_ERROR -> Failure(RemovingBundleError.UNKNOWN_DATABASE_ERROR)
            BundleRemovingError.UNKNOWN_DELETE_ERROR -> Failure(RemovingBundleError.UNKNOWN_DELETE_ERROR)
        }
    }
}

internal fun validateBundleTasks(tasksOrder: List<TaskAndOrder>): Result<List<TaskAndOrder>, CreationBundleTasksError> {
    val tasks = tasksOrder.map { it.task }
    val orders = tasksOrder.map { it.order }
    return when {
        tasks.distinctBy { it.id }.size != tasks.size -> Failure(CreationBundleTasksError.TASKS_NOT_UNIQUE_ERROR)
        orders.distinctBy { it }.size != orders.size -> Failure(CreationBundleTasksError.ORDERS_NOT_UNIQUE_ERROR)
        orders.any { it <= 0 } -> Failure(CreationBundleTasksError.NEGATIVE_ORDERS_ERROR)
        else -> Success(tasksOrder)
    }
}

enum class CreationBundleError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
}

enum class CreationBundleTasksError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
    TASKS_NOT_UNIQUE_ERROR("Bundle tasks must be unique"),
    ORDERS_NOT_UNIQUE_ERROR("Order must be a unique number"),
    NEGATIVE_ORDERS_ERROR("Order must be a unique number"),
    NO_SUCH_BUNDLE_FOR_TASKS("No such bundle to add tasks"),
}

enum class FetchingBundleTasksError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
    NO_SUCH_BUNDLE("No bundle for fetching tasks"),
}

enum class FetchingBundleError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
    NO_SUCH_BUNDLE("No bundle for fetching"),
}

enum class AddBundleError(val errorText: String) {
    USER_HAS_NOT_RIGHTS("Not allowed to add task"),
}

enum class BundleChangingError(val errorText: String) {
    NO_SUCH_BUNDLE("No bundle id to change bundle actuality"),
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
    NO_BUNDLE_ID_FOR_CHANGE("No bundle id to change bundle"),
}

enum class RemovingBundleError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
    NO_SUCH_BUNDLE("The bundle does not exist"),
    UNKNOWN_DELETE_ERROR("Something was wrong until bundle deleting. Please try again later."),
}

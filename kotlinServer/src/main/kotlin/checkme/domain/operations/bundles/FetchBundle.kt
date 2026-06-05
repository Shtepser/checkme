package checkme.domain.operations.bundles

import checkme.domain.models.Bundle
import checkme.domain.models.BundleTasksWithBestResult
import checkme.domain.models.TaskAndOrder
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import org.jooq.exception.DataAccessException
import java.util.UUID

class FetchBundleById(
    private val fetchBundleById: (UUID) -> Bundle?,
) : (UUID) -> Result4k<Bundle, BundleFetchingError> {

    override fun invoke(bundleId: UUID): Result4k<Bundle, BundleFetchingError> =
        try {
            when (val bundle = fetchBundleById(bundleId)) {
                is Bundle -> Success(bundle)
                else -> Failure(BundleFetchingError.NO_SUCH_BUNDLE)
            }
        } catch (_: DataAccessException) {
            Failure(BundleFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchAllBundles(
    private val fetchAllBundles: () -> List<Bundle>?,
) : () -> Result4k<List<Bundle>, BundleFetchingError> {

    override fun invoke(): Result4k<List<Bundle>, BundleFetchingError> =
        try {
            when (val bundles = fetchAllBundles()) {
                is List<Bundle> -> Success(bundles)
                else -> Failure(BundleFetchingError.NO_SUCH_BUNDLE)
            }
        } catch (_: DataAccessException) {
            Failure(BundleFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchAllBundlesByTaskId(
    private val fetchAllBundlesByTaskId: (UUID) -> List<Bundle>?,
) : (UUID) -> Result4k<List<Bundle>, BundleFetchingError> {
    override fun invoke(taskId: UUID): Result4k<List<Bundle>, BundleFetchingError> =
        try {
            when (val bundles = fetchAllBundlesByTaskId(taskId)) {
                is List<Bundle> -> Success(bundles)
                else -> Failure(BundleFetchingError.NO_SUCH_BUNDLE)
            }
        } catch (_: DataAccessException) {
            Failure(BundleFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchHiddenBundles(
    private val fetchHiddenBundles: () -> List<Bundle>?,
) : () -> Result4k<List<Bundle>, BundleFetchingError> {

    override fun invoke(): Result4k<List<Bundle>, BundleFetchingError> =
        try {
            when (val bundles = fetchHiddenBundles()) {
                is List<Bundle> -> Success(bundles)
                else -> Failure(BundleFetchingError.NO_SUCH_BUNDLE)
            }
        } catch (_: DataAccessException) {
            Failure(BundleFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchBundleTasks(
    private val selectBundleTasks: (UUID) -> List<TaskAndOrder>?,
) : (UUID) -> Result4k<List<TaskAndOrder>, BundleFetchingError> {
    override fun invoke(bundleId: UUID): Result4k<List<TaskAndOrder>, BundleFetchingError> {
        return when (val tasks = selectBundleTasks(bundleId)) {
            is List<TaskAndOrder> -> Success(tasks)
            else -> Failure(BundleFetchingError.UNKNOWN_DATABASE_ERROR)
        }
    }
}

class FetchBundleTasksWithUserBestResult(
    private val fetchBundleTasksWithUserBestResult: (Int, UUID) -> List<BundleTasksWithBestResult>?,
) : (Int, UUID) -> Result4k<List<BundleTasksWithBestResult>, BundleFetchingError> {
    override fun invoke(
        page: Int,
        userId: UUID,
    ): Result4k<List<BundleTasksWithBestResult>, BundleFetchingError> {
        return when (val tasks = fetchBundleTasksWithUserBestResult(page, userId)) {
            is List<BundleTasksWithBestResult> -> Success(tasks)
            else -> Failure(BundleFetchingError.UNKNOWN_DATABASE_ERROR)
        }
    }
}

enum class BundleFetchingError {
    UNKNOWN_DATABASE_ERROR,
    NO_SUCH_BUNDLE,
}

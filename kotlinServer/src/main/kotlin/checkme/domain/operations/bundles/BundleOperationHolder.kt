package checkme.domain.operations.bundles

import checkme.domain.models.Bundle
import checkme.domain.models.BundleTasksWithBestResult
import checkme.domain.models.TaskAndOrder
import checkme.domain.operations.dependencies.bundles.BundlesDatabase
import dev.forkhandles.result4k.Result
import java.util.UUID

class BundleOperationHolder(
    private val bundleDatabase: BundlesDatabase,
) {
    val fetchBundleById: (UUID) -> Result<Bundle, BundleFetchingError> =
        FetchBundleById {
                bundleId: UUID,
            ->
            bundleDatabase.selectBundleById(bundleId)
        }

    val fetchAllBundles: () -> Result<List<Bundle>, BundleFetchingError> =
        FetchAllBundles {
            bundleDatabase.selectAllBundles()
        }

    val fetchHiddenBundles: () -> Result<List<Bundle>, BundleFetchingError> =
        FetchHiddenBundles {
            bundleDatabase.selectHiddenBundles()
        }

    val fetchBundleTasksById: (bundleId: UUID) -> Result<List<TaskAndOrder>, BundleFetchingError> =
        FetchBundleTasks {
                bundleId,
            ->
            bundleDatabase.selectBundleTasksById(bundleId)
        }

    val fetchBundlesByTaskId: (taskId: UUID) -> Result<List<Bundle>, BundleFetchingError> =
        FetchAllBundlesByTaskId {
                taskId,
            ->
            bundleDatabase.selectAllBundlesByTaskId(taskId)
        }

    val fetchBundleTasksWithUserBestResult: (page: Int, userId: UUID)
    -> Result<List<BundleTasksWithBestResult>, BundleFetchingError> =
        FetchBundleTasksWithUserBestResult {
                page,
                userId,
            ->
            bundleDatabase.selectAllBundleTasksWithUserBestResult(page, userId)
        }

    val createBundle: (
        name: String,
    ) -> Result<Bundle, CreateBundleError> =
        CreateBundle {
                name: String,
            ->
            bundleDatabase.insertBundle(name)
        }

    val createBundleTasks: (
        bundleId: UUID,
        tasks: List<TaskAndOrder>,
    ) -> Result<List<TaskAndOrder>, CreateBundleTasksError> =
        CreateBundleTasks(
            bundleDatabase::selectBundleById,
            bundleDatabase::insertBundleTasks
        )

    val removeBundle: (bundle: Bundle) -> Result<Boolean, BundleRemovingError> =
        RemoveBundle(
            bundleDatabase::selectBundleById,
            bundleDatabase::deleteBundle
        )

    val modifyBundleActuality: (bundle: Bundle) -> Result<Bundle, ModifyBundleError> =
        ModifyBundleActuality {
                bundle: Bundle,
            ->
            bundleDatabase.updateBundleActuality(bundle)
        }

    val modifyBundle: (bundle: Bundle) -> Result<Bundle, ModifyBundleError> =
        ModifyBundle {
                bundle: Bundle,
            ->
            bundleDatabase.updateBundle(bundle)
        }

    val modifyBundleTasks: (
        bundleId: UUID,
        newTasksAndOrder: List<TaskAndOrder>,
    ) -> Result<List<TaskAndOrder>, ModifyBundleError> =
        ModifyBundleTasks(
            bundleDatabase::selectBundleById,
            bundleDatabase::updateBundleTasks
        )
}

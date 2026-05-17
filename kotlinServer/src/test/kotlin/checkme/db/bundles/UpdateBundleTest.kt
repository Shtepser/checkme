package checkme.db.bundles

import checkme.db.TestcontainerSpec
import checkme.db.tasks.TasksOperations
import checkme.db.validBundles
import checkme.db.validTasks
import checkme.domain.models.Bundle
import checkme.domain.models.TaskAndOrder
import dev.forkhandles.result4k.Success
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class UpdateBundleTest : TestcontainerSpec({ context ->
    val tasksOperations = TasksOperations(context)
    val bundleOperations = BundleOperations(context, tasksOperations)

    lateinit var insertedBundles: List<Bundle>
    lateinit var insertedBundleTasks: List<TaskAndOrder>

    beforeEach {
        for (task in validTasks) {
            tasksOperations.insertTask(
                task.name,
                task.criterions,
                task.answerFormat,
                task.description,
                task.isActual
            ).shouldNotBeNull()
        }
        insertedBundles =
            validBundles.map {
                bundleOperations.insertBundle(
                    it.name,
                ).shouldNotBeNull()
            }
        val tasksIdInDB = tasksOperations.selectAllTask()
        val validBundleTasks: List<TaskAndOrder> = listOf(
            TaskAndOrder(tasksIdInDB[0], 1),
            TaskAndOrder(tasksIdInDB[1], 2),
        )
        insertedBundleTasks =
            bundleOperations.insertBundleTasks(insertedBundles.first().id, validBundleTasks).shouldNotBeNull()
    }

    test("Valid bundle can be removed") {
        val bundleForRemove = validBundles.first()
        bundleOperations.deleteBundle(bundleForRemove).shouldBe(Success(true))
    }

    test("Only one bundle can be deleted") {
        val bundleForRemove = insertedBundles.first()
        bundleOperations.deleteBundle(bundleForRemove).shouldBe(Success(true))
        bundleOperations.selectAllBundles().shouldBe(insertedBundles.subList(1, validBundles.size))
        bundleOperations.deleteBundle(insertedBundles[1]).shouldBe(Success(true))
        bundleOperations.selectAllBundles().shouldBe(insertedBundles.subList(2, validBundles.size))
    }

    test("Bundle can be updated") {
        val newBundle = insertedBundles.first().copy(id = insertedBundles[1].id)
        bundleOperations.updateBundle(newBundle).shouldNotBeNull()
        val updatedBundle = bundleOperations.selectBundleById(insertedBundles[1].id).shouldNotBeNull()

        updatedBundle.id shouldBe insertedBundles[1].id
        updatedBundle.name shouldBe insertedBundles.first().name
        updatedBundle.isActual shouldBe insertedBundles.first().isActual
    }

    test("Bundle actuality can be updated") {
        val bundleWithUpdatedActuality =
            bundleOperations.updateBundleActuality(insertedBundles.first().copy(isActual = false)).shouldNotBeNull()

        bundleWithUpdatedActuality.id shouldBe insertedBundles[0].id
        bundleWithUpdatedActuality.name shouldBe insertedBundles.first().name
        bundleWithUpdatedActuality.isActual shouldBe !insertedBundles.first().isActual
    }

    test("Bundle tasks can be updated") {
        val updatedBundleTasks =
            bundleOperations.updateBundleTasks(
                insertedBundles.first().id,
                listOf(insertedBundleTasks[1], insertedBundleTasks[0])
            ).shouldNotBeNull()

        updatedBundleTasks shouldContainExactlyInAnyOrder listOf(insertedBundleTasks[1], insertedBundleTasks[0])
        updatedBundleTasks.size shouldBe 2
    }
})

package checkme.db.bundles

import checkme.db.TestcontainerSpec
import checkme.db.tasks.TasksOperations
import checkme.db.validBundles
import checkme.db.validTasks
import checkme.db.validUserId
import checkme.domain.models.Bundle
import checkme.domain.models.BundleTasksWithBestResult
import checkme.domain.models.TaskAndOrder
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.util.UUID

class SelectBundleTest : TestcontainerSpec({ context ->
    val tasksOperations = TasksOperations(context)
    val bundleOperations = BundleOperations(context, tasksOperations)

    lateinit var insertedBundles: List<Bundle>
    lateinit var insertedBundleTasksFirst: List<TaskAndOrder>
    lateinit var insertedBundleTasksSecond: List<TaskAndOrder>

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
        insertedBundleTasksFirst =
            bundleOperations.insertBundleTasks(insertedBundles.first().id, validBundleTasks).shouldNotBeNull()
        insertedBundleTasksSecond =
            bundleOperations.insertBundleTasks(insertedBundles[1].id, validBundleTasks).shouldNotBeNull()
    }

    test("Select bundle by id should return this bundle") {
        val selectedBundle = bundleOperations.selectBundleById(insertedBundles.first().id).shouldNotBeNull()

        selectedBundle.id.shouldBe(insertedBundles.first().id)
        selectedBundle.name.shouldBe(insertedBundles.first().name)
        selectedBundle.isActual.shouldBe(true)
    }

    test("Select bundle by invalid id should return null") {
        bundleOperations.selectBundleById(UUID.fromString("019b8ebf-1cba-7736-80a2-b2024d9485db")).shouldBeNull()
    }

    test("Select all bundles should return all of this inserted bundles") {
        val selectedBundles = bundleOperations.selectAllBundles().shouldNotBeNull()
        selectedBundles shouldContainExactlyInAnyOrder insertedBundles
    }

    test("Select hidden bundles from db") {
        bundleOperations
            .selectHiddenBundles()
            .shouldBe(insertedBundles.filter { !it.isActual })
    }

    test("Select bundle tasks should return all of this inserted bundle tasks") {
        val selectedTasks = bundleOperations.selectBundleTasksById(insertedBundles.first().id).shouldNotBeNull()
        selectedTasks shouldContainExactlyInAnyOrder insertedBundleTasksFirst
    }

    test("Select all bundle tasks with user best result should return all not empty bundle with tasks") {
        val selectedTasks = bundleOperations.selectAllBundleTasksWithUserBestResult(1, validUserId[0]).shouldNotBeNull()
        val selectedBundles = bundleOperations.selectAllBundles().shouldNotBeNull().reversed()
        val selectedBundlesWithTasks = mutableListOf<BundleTasksWithBestResult>()
        selectedBundles.forEach {
            val selectedTasks = bundleOperations.selectBundleTasksById(it.id).shouldNotBeNull()
            for (task in selectedTasks) {
                selectedBundlesWithTasks.add(
                    BundleTasksWithBestResult(
                        it.name,
                        task.task.id,
                        task.task.name,
                        0,
                        0,
                    )
                )
            }
        }
        selectedTasks.forEachIndexed { index, task ->
            task.bundleName.shouldBe(selectedBundlesWithTasks[index].bundleName)
            task.taskId.shouldBe(selectedBundlesWithTasks[index].taskId)
            task.taskName.shouldBe(selectedBundlesWithTasks[index].taskName)
        }
    }
})

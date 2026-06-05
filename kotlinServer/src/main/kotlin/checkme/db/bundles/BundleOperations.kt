package checkme.db.bundles

import checkme.db.generated.routines.references.bestSolution
import checkme.db.generated.routines.references.highestScore
import checkme.db.generated.tables.references.BUNDLES
import checkme.db.generated.tables.references.BUNDLE_TASKS
import checkme.db.generated.tables.references.TASKS
import checkme.db.tasks.TasksOperations
import checkme.db.utils.safeLet
import checkme.domain.models.Bundle
import checkme.domain.models.BundleTasksWithBestResult
import checkme.domain.models.TaskAndOrder
import checkme.domain.operations.dependencies.bundles.BundleDatabaseError
import checkme.domain.operations.dependencies.bundles.BundlesDatabase
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.exception.DataAccessException
import org.jooq.exception.IntegrityConstraintViolationException
import org.jooq.impl.DSL
import org.jooq.impl.DSL.commit
import org.jooq.impl.DSL.rollback
import java.util.UUID

const val TASKS_LIMIT = 10

@Suppress("TooManyFunctions")
class BundleOperations(
    private val jooqContext: DSLContext,
    private val taskOperations: TasksOperations,
) : BundlesDatabase {

    override fun selectBundleById(bundleId: UUID): Bundle? =
        selectFromBundles()
            .where(BUNDLES.ID.eq(bundleId))
            .fetchOne()
            ?.toBundle()

    override fun selectAllBundles(): List<Bundle> =
        selectFromBundles()
            .where(BUNDLES.ISACTUAL.eq(true))
            .fetch()
            .mapNotNull { record: Record ->
                record.toBundle()
            }

    override fun selectAllBundlesByTaskId(taskId: UUID): List<Bundle> =
        selectFromBundles()
            .join(BUNDLE_TASKS)
            .on(BUNDLES.ID.eq(BUNDLE_TASKS.BUNDLE_ID))
            .where(BUNDLE_TASKS.TASK_ID.eq(taskId))
            .and(BUNDLES.ISACTUAL.eq(true))
            .fetch()
            .mapNotNull { record: Record ->
                record.toBundle()
            }

    override fun selectAllBundleTasksWithUserBestResult(
        page: Int,
        userId: UUID,
    ): List<BundleTasksWithBestResult>? {
        val taskPage = jooqContext.select(
            BUNDLES.NAME.`as`("bundle_name"),
            BUNDLE_TASKS.TASK_ID,
            TASKS.NAME.`as`("task_name")
        )
            .from(BUNDLES)
            .innerJoin(BUNDLE_TASKS)
            .on(BUNDLES.ID.eq(BUNDLE_TASKS.BUNDLE_ID))
            .innerJoin(TASKS)
            .on(BUNDLE_TASKS.TASK_ID.eq(TASKS.ID))
            .where(
                BUNDLES.ISACTUAL.eq(true)
                    .and(TASKS.IS_ACTUAL.eq(true))
            )
            .orderBy(BUNDLES.ID.desc(), BUNDLE_TASKS.TASK_ORDER.asc())
            .limit(TASKS_LIMIT)
            .offset((page - 1) * TASKS_LIMIT).asTable()

        val taskId = taskPage.field(BUNDLE_TASKS.TASK_ID)

        if (taskId != null) {
            return jooqContext
                .select(
                    taskPage.field("bundle_name", String::class.java),
                    taskId,
                    taskPage.field("task_name", String::class.java),
                    highestScore(taskId),
                    bestSolution(
                        taskId,
                        DSL.`val`(userId)
                    )
                ).from(taskPage)
                .fetch().mapNotNull {
                        record: Record ->
                    record.toTaskAndResult()
                }
        }
        return null
    }

    override fun selectHiddenBundles(): List<Bundle> =
        selectFromBundles()
            .where(BUNDLES.ISACTUAL.eq(false))
            .fetch()
            .mapNotNull { record: Record ->
                record.toBundle()
            }

    override fun selectBundleTasksById(id: UUID): List<TaskAndOrder> = selectBundleTasksByIDRecords(id)

    override fun insertBundle(name: String): Bundle? {
        return jooqContext.insertInto(BUNDLES)
            .set(BUNDLES.NAME, name)
            .set(BUNDLES.ISACTUAL, true)
            .returningResult()
            .fetchOne()
            ?.toBundle()
    }

    override fun insertBundleTasks(
        bundleId: UUID,
        tasksAndOrder: List<TaskAndOrder>,
    ): List<TaskAndOrder>? {
        var savedTasks: List<TaskAndOrder>? = null
        jooqContext.transaction { _ ->
            savedTasks = setTasks(bundleId, tasksAndOrder)
            when {
                savedTasks != null -> commit()
                else -> rollback()
            }
        }

        return savedTasks
    }

    override fun updateBundleActuality(bundle: Bundle): Bundle? {
        return jooqContext.update(BUNDLES)
            .set(BUNDLES.ISACTUAL, bundle.isActual)
            .where(BUNDLES.ID.eq(bundle.id))
            .returningResult()
            .fetchOne()
            ?.toBundle()
    }

    override fun updateBundle(bundle: Bundle): Bundle? {
        return jooqContext.update(BUNDLES)
            .set(BUNDLES.NAME, bundle.name)
            .where(BUNDLES.ID.eq(bundle.id))
            .execute()
            .let { selectBundleById(bundle.id) }
    }

    override fun updateBundleTasks(
        bundleId: UUID,
        newTasksAndOrder: List<TaskAndOrder>,
    ): List<TaskAndOrder>? {
        var savedTasks: List<TaskAndOrder>? = null
        jooqContext.transaction { _ ->
            jooqContext.deleteFrom(BUNDLE_TASKS)
                .where(BUNDLE_TASKS.BUNDLE_ID.eq(bundleId))
                .execute()
            savedTasks = setTasks(bundleId, newTasksAndOrder)
            when {
                savedTasks != null -> commit()
                else -> rollback()
            }
        }

        return savedTasks
    }

    override fun deleteBundle(bundle: Bundle): Result4k<Boolean, BundleDatabaseError> {
        var result: Result4k<Boolean, BundleDatabaseError> = Failure(BundleDatabaseError.UNKNOWN_DATABASE_ERROR)
        jooqContext.transaction { _ ->
            try {
                jooqContext.deleteFrom(BUNDLE_TASKS)
                    .where(BUNDLE_TASKS.BUNDLE_ID.eq(bundle.id))
                    .execute()
                jooqContext.deleteFrom(BUNDLES)
                    .where(BUNDLES.ID.eq(bundle.id))
                    .execute()
                commit()
                result = Success(true)
            } catch (_: DataAccessException) {
                rollback()
            } catch (_: IntegrityConstraintViolationException) {
                rollback()
            }
        }
        return result
    }

    private fun selectBundleTasksByIDRecords(id: UUID) =
        jooqContext.select(
            BUNDLE_TASKS.TASK_ID,
            BUNDLE_TASKS.TASK_ORDER,
        ).from(BUNDLE_TASKS)
            .where(BUNDLE_TASKS.BUNDLE_ID.eq(id))
            .fetch()
            .mapNotNull {
                it.toTaskAndOrder(taskOperations)
            }

    private fun setTasks(
        bundleId: UUID,
        tasksAndOrder: List<TaskAndOrder>,
    ): List<TaskAndOrder>? =
        tasksAndOrder.map { (task, order) ->
            jooqContext
                .insertInto(BUNDLE_TASKS)
                .set(BUNDLE_TASKS.BUNDLE_ID, bundleId)
                .set(BUNDLE_TASKS.TASK_ID, task.id)
                .set(BUNDLE_TASKS.TASK_ORDER, order)
                .returningResult()
                .fetchOne()
                ?.toTaskAndOrder(taskOperations) ?: return null
        }

    private fun selectFromBundles() =
        jooqContext
            .select(
                BUNDLES.ID,
                BUNDLES.NAME,
                BUNDLES.ISACTUAL
            )
            .from(BUNDLES)
}

internal fun Record.toBundle(): Bundle? =
    safeLet(
        this[BUNDLES.ID],
        this[BUNDLES.NAME],
        this[BUNDLES.ISACTUAL],
    ) {
            id,
            name,
            isActual,
        ->
        Bundle(
            id = id,
            name = name,
            isActual = isActual
        )
    }

internal fun Record.toTaskAndOrder(taskOperations: TasksOperations): TaskAndOrder? =
    safeLet(
        this[BUNDLE_TASKS.TASK_ID],
        this[BUNDLE_TASKS.TASK_ORDER],
    ) { taskID, order ->
        taskOperations.selectTaskById(taskID)?.let {
            when {
                it.isActual -> TaskAndOrder(it, order)
                else -> null
            }
        }
    }

internal fun Record.toTaskAndResult(): BundleTasksWithBestResult? =
    safeLet(
        this["bundle_name"] as String,
        this["task_id"] as UUID,
        this["task_name"] as String,
        this["highest_score"] as Int,
        this["best_solution"] as? Int ?: -1
    ) {
            bundleName,
            taskId,
            taskName,
            highestScore,
            bestSolution,
        ->
        BundleTasksWithBestResult(
            bundleName = bundleName,
            taskId = taskId,
            taskName = taskName,
            highestScore = highestScore,
            bestSolution = bestSolution
        )
    }

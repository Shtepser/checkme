@file:Suppress("TooManyFunctions")

package checkme.db.tasks

import checkme.db.checks.CHECKS_LIMIT
import checkme.db.generated.routines.references.bestSolution
import checkme.db.generated.routines.references.highestScore
import checkme.db.generated.tables.references.BUNDLE_TASKS
import checkme.db.generated.tables.references.CHECKS
import checkme.db.generated.tables.references.TASKS
import checkme.db.utils.safeLet
import checkme.domain.checks.Criterion
import checkme.domain.models.AnswerType
import checkme.domain.models.Task
import checkme.domain.operations.dependencies.tasks.TasksDatabase
import checkme.web.solution.forms.TaskIdAndName
import checkme.web.solution.forms.TaskNameForAllResults
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jooq.DSLContext
import org.jooq.JSONB.jsonb
import org.jooq.Record
import org.jooq.impl.DSL
import org.jooq.impl.DSL.commit
import org.jooq.impl.DSL.rollback
import java.util.UUID

class TasksOperations(
    private val jooqContext: DSLContext,
) : TasksDatabase {
    private val objectMapper = jacksonObjectMapper()

    override fun selectTaskById(taskId: UUID): Task? =
        selectFromTasks()
            .where(TASKS.ID.eq(taskId))
            .fetchOne()
            ?.toTask()

    override fun selectTaskByIdWithBestScore(
        taskId: UUID,
        userId: UUID,
    ): Task? =
        jooqContext
            .select(
                TASKS.ID,
                TASKS.NAME,
                TASKS.CRITERIONS,
                TASKS.ANSWERFORMAT,
                TASKS.DESCRIPTION,
                TASKS.IS_ACTUAL,
                bestSolution(TASKS.ID, DSL.`val`(userId)),
                highestScore(TASKS.ID)
            )
            .from(TASKS)
            .where(TASKS.ID.eq(taskId))
            .fetchOne()
            ?.toTaskWithBestScore()

    override fun selectAllTask(): List<Task> =
        selectFromTasks()
            .where(TASKS.IS_ACTUAL.eq(true))
            .orderBy(TASKS.ID)
            .fetch()
            .mapNotNull { record: Record ->
                record.toTask()
            }

    override fun selectAllTasksIdAndNames(): List<TaskIdAndName> =
        selectFromTasks()
            .orderBy(TASKS.ID)
            .fetch()
            .mapNotNull { record: Record ->
                record.toTaskIdAndName()
            }

    override fun selectHiddenTasks(): List<Task> =
        selectFromTasks()
            .where(TASKS.IS_ACTUAL.eq(false))
            .orderBy(TASKS.ID)
            .fetch()
            .mapNotNull { record: Record ->
                record.toTask()
            }

    override fun selectAllTasksPagination(page: Int): List<Task> =
        selectFromTasks()
            .orderBy(TASKS.ID)
            .limit(CHECKS_LIMIT)
            .offset((page - 1) * CHECKS_LIMIT)
            .fetch()
            .mapNotNull { record: Record -> record.toTask() }

    override fun selectTaskName(taskId: UUID): TaskNameForAllResults? =
        jooqContext
            .select(
                TASKS.NAME
            ).from(TASKS)
            .where(TASKS.ID.eq(taskId))
            .fetchOne()
            ?.let { record: Record -> record.toTaskDataForAllResults() }

    override fun insertTask(
        name: String,
        criterions: Map<String, Criterion>,
        answerFormat: Map<String, AnswerType>,
        description: String,
        isActual: Boolean,
    ): Task? {
        return jooqContext.insertInto(TASKS)
            .set(TASKS.NAME, name)
            .set(TASKS.CRITERIONS, jsonb(objectMapper.writeValueAsString(criterions)))
            .set(TASKS.ANSWERFORMAT, jsonb(objectMapper.writeValueAsString(answerFormat)))
            .set(TASKS.DESCRIPTION, description)
            .set(TASKS.IS_ACTUAL, isActual)
            .returningResult()
            .fetchOne()
            ?.toTask()
    }

    override fun deleteTask(taskId: UUID): Int {
        var deleteTaskFlag = 0
        jooqContext.transaction { _ ->
            deleteSolutions(taskId)
            deleteBundleTasks(taskId)
            deleteTaskFlag = jooqContext.delete(TASKS)
                .where(TASKS.ID.eq(taskId))
                .execute()
            when {
                deleteTaskFlag == 1 -> commit()
                else -> rollback()
            }
        }
        return deleteTaskFlag
    }

    override fun updateTask(
        taskId: UUID,
        name: String,
        criterions: Map<String, Criterion>,
        answerFormat: Map<String, AnswerType>,
        description: String,
    ): Task? {
        return jooqContext.update(TASKS)
            .set(TASKS.NAME, name)
            .set(TASKS.CRITERIONS, jsonb(objectMapper.writeValueAsString(criterions)))
            .set(TASKS.ANSWERFORMAT, jsonb(objectMapper.writeValueAsString(answerFormat)))
            .set(TASKS.DESCRIPTION, description)
            .where(TASKS.ID.eq(taskId))
            .returningResult()
            .fetchOne()
            ?.toTask()
    }

    override fun updateTaskActuality(task: Task): Task? {
        return jooqContext.update(TASKS)
            .set(TASKS.IS_ACTUAL, task.isActual)
            .where(TASKS.ID.eq(task.id))
            .returningResult()
            .fetchOne()
            ?.toTask()
    }

    private fun deleteSolutions(taskId: UUID): Int =
        jooqContext.delete(CHECKS)
            .where(CHECKS.TASK_ID.eq(taskId))
            .execute()

    private fun deleteBundleTasks(taskId: UUID): Int =
        jooqContext.delete(BUNDLE_TASKS)
            .where(BUNDLE_TASKS.TASK_ID.eq(taskId))
            .execute()

    private fun selectFromTasks() =
        jooqContext
            .select(
                TASKS.ID,
                TASKS.NAME,
                TASKS.CRITERIONS,
                TASKS.ANSWERFORMAT,
                TASKS.DESCRIPTION,
                TASKS.IS_ACTUAL
            )
            .from(TASKS)
}

internal fun Record.toTask(): Task? =
    safeLet(
        this[TASKS.ID],
        this[TASKS.NAME],
        this[TASKS.CRITERIONS],
        this[TASKS.ANSWERFORMAT],
        this[TASKS.DESCRIPTION],
        this[TASKS.IS_ACTUAL]
    ) {
            id,
            name,
            criterions,
            answerFormat,
            description,
            isActual,
        ->
        Task(
            id = id,
            name = name,
            criterions = jacksonObjectMapper().readValue<Map<String, Criterion>>(criterions.data()),
            answerFormat = jacksonObjectMapper().readValue<Map<String, AnswerType>>(answerFormat.data()),
            description = description,
            isActual = isActual
        )
    }

internal fun Record.toTaskWithBestScore(): Task? =
    safeLet(
        this[TASKS.ID],
        this[TASKS.NAME],
        this[TASKS.CRITERIONS],
        this[TASKS.ANSWERFORMAT],
        this[TASKS.DESCRIPTION],
        this[TASKS.IS_ACTUAL],
        this["best_solution"] as? Int ?: -1,
        this["highest_score"] as Int
    ) {
            id,
            name,
            criterions,
            answerFormat,
            description,
            isActual,
            bestScore,
            highestScore,
        ->
        Task(
            id = id,
            name = name,
            criterions = jacksonObjectMapper().readValue<Map<String, Criterion>>(criterions.data()),
            answerFormat = jacksonObjectMapper().readValue<Map<String, AnswerType>>(answerFormat.data()),
            description = description,
            isActual = isActual,
            bestScore = bestScore,
            highestScore = highestScore
        )
    }

internal fun Record.toTaskDataForAllResults(): TaskNameForAllResults? =
    safeLet(
        this[TASKS.NAME]
    ) {
            name,
        ->
        TaskNameForAllResults(
            name = name,
        )
    }

internal fun Record.toTaskIdAndName(): TaskIdAndName? =
    safeLet(
        this[TASKS.ID],
        this[TASKS.NAME]
    ) {
            id,
            name,
        ->
        TaskIdAndName(
            id = id.toString(),
            name = name,
        )
    }

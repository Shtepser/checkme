package checkme.db.checks

import checkme.db.generated.routines.references.totalScore
import checkme.db.generated.tables.references.CHECKS
import checkme.db.utils.safeLet
import checkme.domain.forms.CheckResult
import checkme.domain.models.Check
import checkme.domain.operations.dependencies.checks.ChecksDatabase
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jooq.DSLContext
import org.jooq.JSONB.jsonb
import org.jooq.Record
import java.time.LocalDateTime
import java.util.UUID

const val CHECKS_LIMIT = 10

class CheckOperations(
    private val jooqContext: DSLContext,
) : ChecksDatabase {
    private val objectMapper = jacksonObjectMapper()

    override fun selectAllChecks(): List<Check> =
        selectFromChecks()
            .fetch()
            .mapNotNull { record: Record ->
                record.toCheckWithScore()
            }

    override fun selectCheckById(checkId: UUID): Check? =
        selectFromChecks()
            .where(CHECKS.ID.eq(checkId))
            .fetchOne()
            ?.toCheckWithScore()

    override fun selectChecksByUserId(userId: UUID): List<Check> =
        selectFromChecks()
            .where(CHECKS.USER_ID.eq(userId))
            .orderBy(CHECKS.ID)
            .fetch()
            .mapNotNull { record: Record ->
                record.toCheckWithScore()
            }

    override fun selectChecksByTaskId(taskId: UUID): List<Check> =
        selectFromChecks()
            .where(CHECKS.TASK_ID.eq(taskId))
            .orderBy(CHECKS.ID)
            .fetch()
            .mapNotNull { record: Record ->
                record.toCheckWithScore()
            }

    override fun selectChecksByUserIdAndTaskId(
        userId: UUID,
        taskId: UUID,
    ): List<Check> =
        selectFromChecks()
            .where(CHECKS.USER_ID.eq(userId).and(CHECKS.TASK_ID.eq(taskId)))
            .orderBy(CHECKS.ID.desc())
            .fetch()
            .mapNotNull { record: Record ->
                record.toCheckWithScore()
            }

    override fun selectAllChecksPagination(page: Int): List<Check> =
        selectFromChecks()
            .orderBy(CHECKS.ID)
            .limit(CHECKS_LIMIT)
            .offset((page - 1) * CHECKS_LIMIT)
            .fetch()
            .mapNotNull { record: Record -> record.toCheckWithScore() }

    override fun updateCheckStatus(
        checkId: UUID,
        status: String,
    ): Check? {
        return jooqContext.update(CHECKS)
            .set(CHECKS.STATUS, status)
            .where(CHECKS.ID.eq(checkId))
            .returningResult()
            .fetchOne()
            ?.toCheck()
    }

    override fun updateCheckResult(
        checkId: UUID,
        result: Map<String, CheckResult>?,
    ): Check? {
        return jooqContext.update(CHECKS)
            .set(CHECKS.RESULT, jsonb(objectMapper.writeValueAsString(result)))
            .where(CHECKS.ID.eq(checkId))
            .returningResult()
            .fetchOne()
            ?.toCheck()
    }

    override fun insertCheck(
        taskId: UUID,
        userId: UUID,
        date: LocalDateTime,
        result: Map<String, CheckResult>?,
        status: String,
    ): Check? {
        return jooqContext.insertInto(CHECKS)
            .set(CHECKS.TASK_ID, taskId)
            .set(CHECKS.USER_ID, userId)
            .set(CHECKS.DATE, date)
            .set(CHECKS.RESULT, jsonb(objectMapper.writeValueAsString(result)))
            .set(CHECKS.STATUS, status)
            .returningResult()
            .fetchOne()
            ?.toCheck()
    }

    private fun selectFromChecks() =
        jooqContext
            .select(
                CHECKS.ID,
                CHECKS.TASK_ID,
                CHECKS.USER_ID,
                CHECKS.DATE,
                CHECKS.RESULT,
                CHECKS.STATUS,
                totalScore(CHECKS.ID)
            )
            .from(CHECKS)
}

internal fun Record.toCheck(): Check? =
    safeLet(
        this[CHECKS.ID],
        this[CHECKS.TASK_ID],
        this[CHECKS.USER_ID],
        this[CHECKS.DATE],
        this[CHECKS.RESULT],
        this[CHECKS.STATUS]
    ) {
            id,
            taskId,
            userId,
            date,
            result,
            status,
        ->
        Check(
            id = id,
            taskId = taskId,
            userId = userId,
            date = date,
            result = jacksonObjectMapper().readValue<Map<String, CheckResult>>(result.data()),
            status = status,
        )
    }

internal fun Record.toCheckWithScore(): Check? =
    safeLet(
        this[CHECKS.ID],
        this[CHECKS.TASK_ID],
        this[CHECKS.USER_ID],
        this[CHECKS.DATE],
        this[CHECKS.RESULT],
        this[CHECKS.STATUS],
        this["total_score"] as Int,
    ) {
            id,
            taskId,
            userId,
            date,
            result,
            status,
            score,
        ->
        Check(
            id = id,
            taskId = taskId,
            userId = userId,
            date = date,
            result = jacksonObjectMapper().readValue<Map<String, CheckResult>>(result.data()),
            status = status,
            totalScore = score
        )
    }

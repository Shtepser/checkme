package checkme.domain.operations.dependencies.checks

import checkme.domain.forms.CheckResult
import checkme.domain.models.Check
import java.time.LocalDateTime
import java.util.UUID

interface ChecksDatabase {
    fun selectCheckById(checkId: UUID): Check?

    fun selectChecksByUserId(userId: UUID): List<Check>

    fun selectChecksByTaskId(taskId: UUID): List<Check>

    fun selectChecksByUserIdAndTaskId(
        userId: UUID,
        taskId: UUID,
    ): List<Check>

    fun selectAllChecks(): List<Check>

    fun selectAllChecksPagination(page: Int): List<Check>

    fun insertCheck(
        taskId: UUID,
        userId: UUID,
        date: LocalDateTime,
        result: Map<String, CheckResult>?,
        status: String,
    ): Check?

    fun updateCheckStatus(
        checkId: UUID,
        status: String,
    ): Check?

    fun updateCheckResult(
        checkId: UUID,
        result: Map<String, CheckResult>?,
    ): Check?
}

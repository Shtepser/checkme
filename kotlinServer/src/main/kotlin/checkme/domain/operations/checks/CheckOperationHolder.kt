package checkme.domain.operations.checks

import checkme.domain.forms.CheckResult
import checkme.domain.models.Check
import checkme.domain.operations.dependencies.checks.ChecksDatabase
import dev.forkhandles.result4k.Result
import java.time.LocalDateTime
import java.util.UUID

class CheckOperationHolder (
    private val checksDatabase: ChecksDatabase,
) {
    val fetchCheckById: (UUID) -> Result<Check, CheckFetchingError> =
        FetchCheckById {
                checkId: UUID ->
            checksDatabase.selectCheckById(checkId)
        }

    val fetchAllChecksPagination: (Int) -> Result<List<Check>, CheckFetchingError> =
        FetchAllChecksPagination {
                page: Int ->
            checksDatabase.selectAllChecksPagination(page)
        }

    val fetchAllChecks: () -> Result<List<Check>, CheckFetchingError> =
        FetchAllChecks {
            checksDatabase.selectAllChecks()
        }

    val fetchChecksByUserId: (UUID) -> Result<List<Check>, CheckFetchingError> =
        FetchChecksByUserId {
                userId: UUID,
            ->
            checksDatabase.selectChecksByUserId(userId)
        }

    val fetchChecksByTaskId: (UUID) -> Result<List<Check>, CheckFetchingError> =
        FetchChecksByTaskId {
                taskId: UUID,
            ->
            checksDatabase.selectChecksByTaskId(taskId)
        }

    val fetchChecksByUserIdAndTaskId: (UUID, UUID) -> Result<List<Check>, CheckFetchingError> =
        FetchChecksByUserIdAndTaskId {
                userId: UUID,
                taskId: UUID,
            ->
            checksDatabase.selectChecksByUserIdAndTaskId(userId, taskId)
        }

    val createCheck: (
        taskId: UUID,
        userId: UUID,
        date: LocalDateTime,
        result: Map<String, CheckResult>,
        status: String,
    ) -> Result<Check, CreateCheckError> =
        CreateCheck {
                taskId: UUID,
                userId: UUID,
                date: LocalDateTime,
                result: Map<String, CheckResult>,
                status: String,
            ->
            checksDatabase.insertCheck(
                taskId = taskId,
                userId = userId,
                date = date,
                result = result,
                status = status
            )
        }

    val updateCheckStatus: (
        checkId: UUID,
        status: String,
    ) -> Result<Check, ModifyCheckError> =
        ModifyCheckStatus {
                checkId: UUID,
                status: String,
            ->
            checksDatabase.updateCheckStatus(checkId, status)
        }

    val updateCheckResult: (
        checkId: UUID,
        result: Map<String, CheckResult>,
    ) -> Result<Check, ModifyCheckError> =
        ModifyCheckResult {
                checkId: UUID,
                result: Map<String, CheckResult>,
            ->
            checksDatabase.updateCheckResult(checkId, result)
        }
}

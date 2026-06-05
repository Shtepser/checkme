package checkme.domain.operations.checks

import checkme.domain.models.Check
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import org.jooq.exception.DataAccessException
import java.util.UUID

class FetchCheckById(
    private val fetchCheckById: (UUID) -> Check?,
) : (UUID) -> Result4k<Check, CheckFetchingError> {

    override fun invoke(checkId: UUID): Result4k<Check, CheckFetchingError> =
        try {
            when (val check = fetchCheckById(checkId)) {
                is Check -> Success(check)
                else -> Failure(CheckFetchingError.NO_SUCH_CHECK)
            }
        } catch (_: DataAccessException) {
            Failure(CheckFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchChecksByUserId(
    private val fetchChecksByUserId: (UUID) -> List<Check>?,
) : (UUID) -> Result4k<List<Check>, CheckFetchingError> {

    override fun invoke(userId: UUID): Result4k<List<Check>, CheckFetchingError> =
        try {
            when (val checks = fetchChecksByUserId(userId)) {
                is List<Check> -> Success(checks)
                else -> Failure(CheckFetchingError.NO_SUCH_CHECK)
            }
        } catch (_: DataAccessException) {
            Failure(CheckFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchChecksByTaskId(
    private val fetchChecksByTaskId: (UUID) -> List<Check>?,
) : (UUID) -> Result4k<List<Check>, CheckFetchingError> {

    override fun invoke(taskId: UUID): Result4k<List<Check>, CheckFetchingError> =
        try {
            when (val checks = fetchChecksByTaskId(taskId)) {
                is List<Check> -> Success(checks)
                else -> Failure(CheckFetchingError.NO_SUCH_CHECK)
            }
        } catch (_: DataAccessException) {
            Failure(CheckFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchChecksByUserIdAndTaskId(
    private val fetchChecksByUserIdAndTaskId: (UUID, UUID) -> List<Check>?,
) : (UUID, UUID) -> Result4k<List<Check>, CheckFetchingError> {

    override fun invoke(
        userId: UUID,
        taskId: UUID,
    ): Result4k<List<Check>, CheckFetchingError> =
        try {
            when (val checks = fetchChecksByUserIdAndTaskId(userId, taskId)) {
                is List<Check> -> Success(checks)
                else -> Failure(CheckFetchingError.NO_SUCH_CHECK)
            }
        } catch (_: DataAccessException) {
            Failure(CheckFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchAllChecksPagination(
    private val fetchAllChecksWithData: (Int) -> List<Check>?,
) : (Int) -> Result4k<List<Check>, CheckFetchingError> {

    override fun invoke(page: Int): Result4k<List<Check>, CheckFetchingError> =
        try {
            when (val checks = fetchAllChecksWithData(page)) {
                is List<Check> -> Success(checks)
                else -> Failure(CheckFetchingError.NO_SUCH_CHECK)
            }
        } catch (_: DataAccessException) {
            Failure(CheckFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchAllChecks(
    private val fetchAllChecks: () -> List<Check>?,
) : () -> Result4k<List<Check>, CheckFetchingError> {

    override fun invoke(): Result4k<List<Check>, CheckFetchingError> =
        try {
            when (val checks = fetchAllChecks()) {
                is List<Check> -> Success(checks)
                else -> Failure(CheckFetchingError.NO_SUCH_CHECK)
            }
        } catch (_: DataAccessException) {
            Failure(CheckFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

enum class CheckFetchingError {
    UNKNOWN_DATABASE_ERROR,
    NO_SUCH_CHECK,
}

package checkme.db.checks

import checkme.db.TestcontainerSpec
import checkme.db.validChecks
import checkme.db.validChecksMany
import checkme.domain.models.Check
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.util.UUID

class SelectCheckTest : TestcontainerSpec({ context ->
    val checkOperations = CheckOperations(context)

    val notExistingId = UUID.fromString("00000000-0000-7736-80a2-b2024d9485db")

    lateinit var insertedChecks: List<Check>

    beforeEach {
        insertedChecks =
            validChecks.map {
                checkOperations.insertCheck(
                    it.taskId,
                    it.userId,
                    it.date,
                    it.result,
                    it.status
                ).shouldNotBeNull()
            }
    }

    test("Select check by id should return this check") {
        val selectedCheck = checkOperations.selectCheckById(insertedChecks.first().id).shouldNotBeNull()

        selectedCheck.id.shouldBe(insertedChecks.first().id)
        selectedCheck.taskId.shouldBe(insertedChecks.first().taskId)
        selectedCheck.userId.shouldBe(insertedChecks.first().userId)
        selectedCheck.date.shouldBe(insertedChecks.first().date)
        selectedCheck.result.shouldBe(insertedChecks.first().result)
        selectedCheck.status.shouldBe(insertedChecks.first().status)
    }

    test("Select check by invalid id should return null") {
        checkOperations.selectCheckById(notExistingId).shouldBeNull()
    }

    test("Select checks by valid userId should return this checks") {
        val selectedChecks = checkOperations.selectChecksByUserId(insertedChecks.first().userId).shouldNotBeNull()
        selectedChecks.map {
            Check(it.id, it.taskId, it.userId, it.date, it.result, it.status, null)
        } shouldContainExactlyInAnyOrder insertedChecks.filter { it.userId == insertedChecks.first().userId }
    }

    test("Select checks by invalid userId should return empty list") {
        checkOperations.selectChecksByUserId(notExistingId)
            .shouldBeEmpty()
    }

    test("Select checks by valid taskId should return this checks") {
        val selectedChecks = checkOperations.selectChecksByTaskId(insertedChecks.first().taskId).shouldNotBeNull()
        selectedChecks.map {
            Check(it.id, it.taskId, it.userId, it.date, it.result, it.status, null)
        } shouldContainExactlyInAnyOrder insertedChecks
            .filter { it.taskId == insertedChecks.first().taskId }
    }

    test("Select checks by invalid taskId should return empty list") {
        checkOperations.selectChecksByTaskId(notExistingId)
            .shouldBeEmpty()
    }

    test("Select all checks should return all of this inserted checks") {
        val selectedChecks = checkOperations.selectAllChecks().shouldNotBeNull()
        selectedChecks.map {
            Check(it.id, it.taskId, it.userId, it.date, it.result, it.status, null)
        } shouldContainExactlyInAnyOrder insertedChecks
    }

    test(
        "Select all checks pagination should return a list with the size of the inserted checks if " +
            "there are less than 10"
    ) {
        val selectedChecksBefore = checkOperations.selectAllChecksPagination(1).shouldNotBeNull()
        selectedChecksBefore.size.shouldBe(insertedChecks.size)
    }

    test("Select all checks pagination should return a list with the size 10 if this count exists") {
        validChecksMany.map {
            checkOperations.insertCheck(
                it.taskId,
                it.userId,
                it.date,
                it.result,
                it.status
            ).shouldNotBeNull()
        }
        val selectedChecksBefore = checkOperations.selectAllChecksPagination(1).shouldNotBeNull()
        selectedChecksBefore.size.shouldBe(10)
    }

    test("Select checks by valid userId and taskId should return this checks") {
        val selectedChecks = checkOperations
            .selectChecksByUserIdAndTaskId(
                insertedChecks.first().userId,
                insertedChecks.first().taskId
            ).shouldNotBeNull()
        selectedChecks.map {
            Check(it.id, it.taskId, it.userId, it.date, it.result, it.status, null)
        } shouldContainExactlyInAnyOrder insertedChecks
            .filter { (it.userId == insertedChecks.first().userId) && (it.taskId == insertedChecks.first().taskId) }
    }

    test("Select checks by invalid userId and valid taskId should return empty list") {
        checkOperations
            .selectChecksByUserIdAndTaskId(
                notExistingId,
                insertedChecks.first().taskId
            ).shouldBeEmpty()
    }

    test("Select checks by valid userId and invalid taskId should return empty list") {
        checkOperations
            .selectChecksByUserIdAndTaskId(
                insertedChecks.first().userId,
                notExistingId
            ).shouldBeEmpty()
    }

    test("Select checks by invalid userId and invalid taskId should return empty list") {
        checkOperations
            .selectChecksByUserIdAndTaskId(
                notExistingId,
                notExistingId
            ).shouldBeEmpty()
    }
})

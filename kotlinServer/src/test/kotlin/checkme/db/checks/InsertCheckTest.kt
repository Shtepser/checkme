package checkme.db.checks

import checkme.db.TestcontainerSpec
import checkme.db.appConfiguredPasswordHasher
import checkme.db.selectUserId
import checkme.db.users.UserOperations
import checkme.db.validAdminLogin
import checkme.db.validChecks
import checkme.db.validLogin
import checkme.db.validName
import checkme.db.validPass
import checkme.db.validSurname
import checkme.domain.accounts.Role
import checkme.domain.models.Check
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class InsertCheckTest : TestcontainerSpec({ context ->
    val checkOperations = CheckOperations(context)
    val userOperations = UserOperations(context)

    val validChecksNew = mutableListOf<Check>()

    beforeEach {
        userOperations
            .insertUser(
                validLogin,
                validName,
                validSurname,
                appConfiguredPasswordHasher.hash(validPass),
                Role.STUDENT,
            ).shouldNotBeNull()

        userOperations
            .insertUser(
                validAdminLogin,
                validName,
                validSurname,
                appConfiguredPasswordHasher.hash(validPass),
                Role.ADMIN,
            ).shouldNotBeNull()

        userOperations
            .insertUser(
                validLogin + "1",
                validName,
                validSurname,
                appConfiguredPasswordHasher.hash(validPass + "2"),
                Role.STUDENT,
            ).shouldNotBeNull()

        val users = userOperations.selectAllUsers().map { it.id }

        validChecks.forEach { check ->
            validChecksNew.add(
                Check(
                    check.id,
                    check.taskId,
                    selectUserId(check.userId, users),
                    check.date,
                    check.result,
                    check.status,
                    check.totalScore
                )
            )
        }
    }

    afterEach {
        validChecksNew.clear()
    }

    test("Valid check insertions should return this check") {
        val checkForInsert = validChecksNew.first()
        val insertedCheck =
            checkOperations.insertCheck(
                checkForInsert.taskId,
                checkForInsert.userId,
                checkForInsert.date,
                checkForInsert.result,
                checkForInsert.status,
            ).shouldNotBeNull()

        insertedCheck.taskId.shouldBe(checkForInsert.taskId)
        insertedCheck.userId.shouldBe(checkForInsert.userId)
        insertedCheck.date.shouldBe(checkForInsert.date)
        insertedCheck.result.shouldBe(checkForInsert.result)
        insertedCheck.status.shouldBe(checkForInsert.status)
    }

    test("Valid check with empty result can be inserted") {
        val checkForInsert = validChecksNew.filter { it.result.isEmpty() }.first()
        val insertedCheck =
            checkOperations.insertCheck(
                checkForInsert.taskId,
                checkForInsert.userId,
                checkForInsert.date,
                checkForInsert.result,
                checkForInsert.status,
            ).shouldNotBeNull()

        insertedCheck.taskId.shouldBe(checkForInsert.taskId)
        insertedCheck.userId.shouldBe(checkForInsert.userId)
        insertedCheck.date.shouldBe(checkForInsert.date)
        insertedCheck.result.shouldBe(checkForInsert.result)
        insertedCheck.status.shouldBe(checkForInsert.status)
    }
})

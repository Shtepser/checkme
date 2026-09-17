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
import checkme.db.validResult
import checkme.db.validStatusCorrect
import checkme.db.validSurname
import checkme.domain.accounts.Role
import checkme.domain.models.Check
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class UpdateCheckTest : TestcontainerSpec({ context ->
    val checkOperations = CheckOperations(context)
    val userOperations = UserOperations(context)

    val validChecksNew = mutableListOf<Check>()

    lateinit var insertedChecks: List<Check>

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

        insertedChecks =
            validChecksNew.map {
                checkOperations.insertCheck(
                    it.taskId,
                    it.userId,
                    it.date,
                    it.result,
                    it.status
                ).shouldNotBeNull()
            }
    }

    afterEach {
        validChecksNew.clear()
    }

    test("Check result can be updated") {
        val insertedCheck = insertedChecks.first { it.result.isEmpty() }
        checkOperations.updateCheckResult(insertedCheck.id, validResult).shouldNotBeNull()

        val updatedCheck = checkOperations.selectCheckById(insertedCheck.id).shouldNotBeNull()

        updatedCheck.id shouldBe insertedCheck.id
        updatedCheck.userId shouldBe insertedCheck.userId
        updatedCheck.taskId shouldBe insertedCheck.taskId
        updatedCheck.date shouldBe insertedCheck.date
        updatedCheck.result shouldBe validResult
        updatedCheck.status shouldBe insertedCheck.status
    }

    test("Check status can be updated") {
        val insertedCheck = insertedChecks.first { it.status != validStatusCorrect }
        checkOperations.updateCheckStatus(insertedCheck.id, validStatusCorrect).shouldNotBeNull()

        val updatedCheck = checkOperations.selectCheckById(insertedCheck.id).shouldNotBeNull()

        updatedCheck.id shouldBe insertedCheck.id
        updatedCheck.userId shouldBe insertedCheck.userId
        updatedCheck.taskId shouldBe insertedCheck.taskId
        updatedCheck.date shouldBe insertedCheck.date
        updatedCheck.result shouldBe insertedCheck.result
        updatedCheck.status shouldBe validStatusCorrect
    }
})

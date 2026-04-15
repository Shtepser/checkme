package checkme.domain.models

import checkme.config.CheckDatabaseConfig
import checkme.config.LoggingConfig
import checkme.domain.checks.CheckDataConsole
import checkme.domain.checks.CheckDataSQL
import checkme.domain.checks.ConsoleCheckTest
import checkme.domain.checks.Criterion
import checkme.domain.checks.SqlCheckTest
import checkme.domain.forms.CheckResult
import checkme.logging.LoggerType
import checkme.logging.ServerLogger
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.time.LocalDateTime
import java.util.UUID

@Suppress("LongParameterList")
data class Check(
    val id: UUID,
    val taskId: UUID,
    val userId: UUID,
    val date: LocalDateTime,
    val result: Map<String, CheckResult>,
    val status: String,
    val totalScore: Int? = null,
) {
    companion object {
        private val specialCriteria = listOf("beforeAll", "beforeEach", "afterEach", "afterAll")

        internal fun checkStudentAnswer(
            task: Task,
            checkId: UUID,
            user: User,
            answers: List<Pair<String, String>>,
            checkDatabaseConfig: CheckDatabaseConfig,
            loggingConfig: LoggingConfig,
        ): Map<String, CheckResult>? {
            val results = mutableMapOf<String, CheckResult>()
            beforeAllCriterionCheck(
                task = task,
                checkId = checkId,
                user = user,
                answers = answers,
                results = results,
                checkDatabaseConfig = checkDatabaseConfig,
                loggingConfig = loggingConfig
            )
            for (criterion in task.criterions.filter { !specialCriteria.contains(it.value.specialMarker?.code) }) {
                beforeEachCriterionCheck(
                    task = task,
                    checkId = checkId,
                    user = user,
                    answers = answers,
                    results = results,
                    checkDatabaseConfig = checkDatabaseConfig,
                    loggingConfig = loggingConfig
                )
                if (!specialCriteria.contains(criterion.value.specialMarker?.code)) {
                    val checkResult = criterionCheck(
                        criterion = criterion,
                        task = task,
                        checkId = checkId,
                        user = user,
                        answers = answers,
                        checkDatabaseConfig = checkDatabaseConfig,
                        loggingConfig = loggingConfig
                    ) ?: return null
                    results[criterion.key] = checkResult
                }
                afterEachCriterionCheck(
                    task = task,
                    checkId = checkId,
                    user = user,
                    answers = answers,
                    results = results,
                    checkDatabaseConfig = checkDatabaseConfig,
                    loggingConfig = loggingConfig
                )
            }
            afterAllCriterionCheck(
                task = task,
                checkId = checkId,
                user = user,
                answers = answers,
                results = results,
                checkDatabaseConfig = checkDatabaseConfig,
                loggingConfig = loggingConfig
            )
            return results
        }

        private fun beforeAllCriterionCheck(
            task: Task,
            checkId: UUID,
            user: User,
            answers: List<Pair<String, String>>,
            results: MutableMap<String, CheckResult>,
            checkDatabaseConfig: CheckDatabaseConfig,
            loggingConfig: LoggingConfig,
        ) {
            val specialResultBeforeAll = tryCheckSpecialCriterionAll(
                specialCriterion = task.criterions.entries.firstOrNull { it.value.specialMarker?.code == "beforeAll" },
                task = task,
                checkId = checkId,
                user = user,
                answers = answers,
                checkDatabaseConfig = checkDatabaseConfig,
                loggingConfig = loggingConfig
            )
            if (specialResultBeforeAll != null) {
                results[specialResultBeforeAll.first] = specialResultBeforeAll.second
            }
        }

        private fun beforeEachCriterionCheck(
            task: Task,
            checkId: UUID,
            user: User,
            answers: List<Pair<String, String>>,
            results: MutableMap<String, CheckResult>,
            checkDatabaseConfig: CheckDatabaseConfig,
            loggingConfig: LoggingConfig,
        ) {
            val specialResultBeforeEach = results.tryCheckSpecialCriterionEach(
                specialCriterion = task.criterions.entries.firstOrNull { it.value.specialMarker?.code == "beforeEach" },
                task = task,
                checkId = checkId,
                user = user,
                answers = answers,
                checkDatabaseConfig = checkDatabaseConfig,
                loggingConfig = loggingConfig
            )

            if (specialResultBeforeEach != null) {
                results[specialResultBeforeEach.first] = specialResultBeforeEach.second
            }
        }

        private fun afterAllCriterionCheck(
            task: Task,
            checkId: UUID,
            user: User,
            answers: List<Pair<String, String>>,
            results: MutableMap<String, CheckResult>,
            checkDatabaseConfig: CheckDatabaseConfig,
            loggingConfig: LoggingConfig,
        ) {
            val specialResultAfterAll = tryCheckSpecialCriterionAll(
                specialCriterion = task.criterions.entries.firstOrNull { it.value.specialMarker?.code == "afterAll" },
                task = task,
                checkId = checkId,
                user = user,
                answers = answers,
                checkDatabaseConfig = checkDatabaseConfig,
                loggingConfig = loggingConfig
            )
            if (specialResultAfterAll != null) {
                results[specialResultAfterAll.first] = specialResultAfterAll.second
            }
        }

        private fun afterEachCriterionCheck(
            task: Task,
            checkId: UUID,
            user: User,
            answers: List<Pair<String, String>>,
            results: MutableMap<String, CheckResult>,
            checkDatabaseConfig: CheckDatabaseConfig,
            loggingConfig: LoggingConfig,
        ) {
            val specialResultAfterEach = results.tryCheckSpecialCriterionEach(
                specialCriterion = task.criterions.entries.firstOrNull { it.value.specialMarker?.code == "afterEach" },
                task = task,
                checkId = checkId,
                user = user,
                answers = answers,
                checkDatabaseConfig = checkDatabaseConfig,
                loggingConfig = loggingConfig
            )

            if (specialResultAfterEach != null) {
                results[specialResultAfterEach.first] = specialResultAfterEach.second
            }
        }

        private fun MutableMap<String, CheckResult>.tryCheckSpecialCriterionEach(
            specialCriterion: Map.Entry<String, Criterion>?,
            task: Task,
            checkId: UUID,
            user: User,
            answers: List<Pair<String, String>>,
            checkDatabaseConfig: CheckDatabaseConfig,
            loggingConfig: LoggingConfig,
        ): Pair<String, CheckResult>? {
            return if (
                (
                        (this.criterionAlreadyChecked(specialCriterion)) ||
                                (this[specialCriterion?.key] == null)
                        ) &&
                specialCriterion != null
            ) {
                when (
                    val eachResult =
                        criterionCheck(
                            criterion = specialCriterion,
                            task = task,
                            checkId = checkId,
                            user = user,
                            answers = answers,
                            checkDatabaseConfig = checkDatabaseConfig,
                            loggingConfig = loggingConfig
                        )
                ) {
                    is CheckResult -> Pair(specialCriterion.key, eachResult)
                    else -> null
                }
            } else {
                null
            }
        }

        private fun tryCheckSpecialCriterionAll(
            specialCriterion: Map.Entry<String, Criterion>?,
            task: Task,
            checkId: UUID,
            user: User,
            answers: List<Pair<String, String>>,
            checkDatabaseConfig: CheckDatabaseConfig,
            loggingConfig: LoggingConfig,
        ): Pair<String, CheckResult>? {
            val allResult = specialCriterion
                ?.let {
                    criterionCheck(
                        criterion = it,
                        task = task,
                        checkId = checkId,
                        user = user,
                        answers = answers,
                        checkDatabaseConfig = checkDatabaseConfig,
                        loggingConfig = loggingConfig
                    )
                }
                ?: return null
            return Pair(specialCriterion.key, allResult)
        }

        @Suppress("UnusedParameter", "LongMethod")
        private fun criterionCheck(
            criterion: Map.Entry<String, Criterion>,
            task: Task,
            checkId: UUID,
            user: User,
            answers: List<Pair<String, String>>,
            checkDatabaseConfig: CheckDatabaseConfig,
            loggingConfig: LoggingConfig,
        ): CheckResult? {
            // todo answers могут понадобиться для следующих проверок
            val testConfig = criterion.value.test
            return when (testConfig) {
                is ConsoleCheckTest -> {
                    val check = CheckDataConsole(
                        type = CheckType.CONSOLE_CHECK,
                        command = testConfig.command,
                        expected = testConfig.expected
                    )
                    CheckDataConsole.consoleCheck(task, check, user, checkId, criterion.value)
                }

                is SqlCheckTest -> {
                    val dbScript = testConfig.dbScript
                    val scripts = dbScript.split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                    val check = CheckDataSQL(
                        type = CheckType.SQL_CHECK,
                        dbScript = scripts,
                        referenceQuery = testConfig.referenceQuery
                    )
                    CheckDataSQL.sqlCheck(
                        task = task,
                        checkDataSQL = check,
                        user = user,
                        checkId = checkId,
                        criterion = criterion.value,
                        overall = loggingConfig.overall,
                        config = checkDatabaseConfig
                    )
                }
            }
        }
    }
}

private fun MutableMap<String, CheckResult>.criterionAlreadyChecked(specialCriterion: Map.Entry<String, Criterion>?) =
    this[specialCriterion?.key] != null && this[specialCriterion?.key]?.score != 0

enum class CheckType(val code: String) {
    CONSOLE_CHECK("console-check"),
    SQL_CHECK("sql-check"),
}

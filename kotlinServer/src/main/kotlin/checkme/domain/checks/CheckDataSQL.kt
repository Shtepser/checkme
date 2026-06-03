package checkme.domain.checks

import checkme.config.CheckDatabaseConfig
import checkme.domain.forms.CheckResult
import checkme.domain.models.CheckType
import checkme.domain.models.Task
import checkme.domain.models.User
import checkme.logging.LoggerType
import checkme.logging.ServerLogger
import checkme.service.SqlCheckService
import checkme.web.solution.handlers.SOLUTIONS_DIR
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import java.io.File
import java.util.UUID

@Suppress("LongParameterList")
data class CheckDataSQL(
    val type: CheckType,
    val dbScript: List<String>,
    val referenceQuery: String,
) {
    companion object {
        fun sqlCheck(
            task: Task,
            checkDataSQL: CheckDataSQL,
            user: User,
            checkId: UUID,
            criterion: Criterion,
            overall: Boolean,
            config: CheckDatabaseConfig,
        ): CheckResult {
            val directoryPath = "..$SOLUTIONS_DIR" +
                "/${user.name}-${user.surname}-${user.login}" +
                "/${task.name}" +
                "/$checkId"
            if (!File(directoryPath).exists()) {
                ServerLogger.log(
                    user = user,
                    action = "Check task warnings",
                    message = "Check failed, file for solution (check $checkId) not found",
                    type = LoggerType.WARN
                )
                return CheckResult(0, "Check failed, file for solution check $checkId not found")
            }
            val answerFile = File(directoryPath).listFiles()?.firstOrNull { it.extension == "sql" }
            return when {
                answerFile == null ->
                    CheckResult(0, "Check failed, file for solution check is not a sql-file")

                else -> {
                    tryGetSQLDataAndCheckResults(
                        answerFile = answerFile,
                        checkDataSQL = checkDataSQL,
                        task = task,
                        user = user,
                        checkId = checkId,
                        criterion = criterion,
                        overall = overall,
                        config = config
                    )
                }
            }
        }

        private fun findCScriptFile(
            scriptNames: List<String>,
            taskName: String,
        ): List<File>? {
            val dir = File("../tasks/$taskName")
            if (!dir.isDirectory) return null
            return dir.listFiles()?.filter { scriptNames.contains(it.name) }
        }

        private fun tryGetSQLDataAndCheckResults(
            answerFile: File,
            checkDataSQL: CheckDataSQL,
            task: Task,
            user: User,
            checkId: UUID,
            criterion: Criterion,
            overall: Boolean,
            config: CheckDatabaseConfig,
        ): CheckResult {
            val answerSQl = answerFile.readText()
            val sqlScripts = findCScriptFile(
                scriptNames = checkDataSQL.dbScript,
                taskName = task.name
            )
            if (sqlScripts != null) {
                for (script in sqlScripts)
                    println(script.name)
            }
            return when {
                sqlScripts == null || !sqlScripts.all { it.exists() } -> {
                    ServerLogger.log(
                        user = user,
                        action = "Check task warnings",
                        message =
                            """
                            Check failed: Setup SQL script ${checkDataSQL.dbScript} for 
                            task ${task.id}-${task.name} not found
                            """.trimIndent(),
                        type = LoggerType.WARN
                    )
                    CheckResult(
                        0,
                        """
                        Check failed: Setup SQL script ${checkDataSQL.dbScript} for
                        task ${task.id}-${task.name} not found
                        """.trimIndent()
                    )
                }

                else -> {
                    getCheckResults(
                        task = task,
                        checkDataSQL = checkDataSQL,
                        studentQuery = answerSQl,
                        user = user,
                        checkId = checkId,
                        criterion = criterion,
                        overall = overall,
                        config = config,
                        setupSql = sqlScripts
                    )
                }
            }
        }

        @Suppress("LongMethod")
        private fun getCheckResults(
            task: Task,
            checkDataSQL: CheckDataSQL,
            studentQuery: String,
            user: User,
            checkId: UUID,
            criterion: Criterion,
            overall: Boolean,
            config: CheckDatabaseConfig,
            setupSql: List<File>,
        ): CheckResult {
            val scriptsResult: MutableList<CheckResult> = mutableListOf()
            for (script in setupSql) {
                when (
                    val queriesResults = SqlCheckService(config, user, overall).getSqlResults(
                        firstScript = script,
                        referenceQuery = checkDataSQL.referenceQuery,
                        studentQuery = studentQuery,
                        checkId = checkId,
                    )
                ) {
                    is Failure -> {
                        ServerLogger.log(
                            user = user,
                            action = "Check task warnings",
                            message =
                                """
                                An error occurred while running check ${criterion.test} for task
                                ${task.name}-${task.id}: ${queriesResults.reason.trim()}
                                script: ${script.name}"
                                """.trimIndent(),
                            type = LoggerType.WARN
                        )
                        return CheckResult(
                            0,
                            """
                            An error occurred while running check ${criterion.test} for task
                            ${task.name}-${task.id}: ${queriesResults.reason.trim()}
                            script: ${script.name}
                            """.trimIndent()
                        )
                    }

                    is Success -> {
                        val studentResult = queriesResults.value.first
                        val referenceResult = queriesResults.value.second
                        val studentLines = studentResult.lines().toList()
                        val referenceLines = referenceResult.lines().toList()
                        val studentColumnsCount = studentLines.first().split("|").size
                        val referenceColumnsCount = referenceLines.first().split("|").size
                        when {
                            studentResult == referenceResult -> scriptsResult.add(
                                CheckResult(
                                    criterion.score,
                                    criterion.description
                                )
                            )

                            studentColumnsCount > referenceColumnsCount -> scriptsResult.add(
                                CheckResult(
                                    0,
                                    "${criterion.message}. Result contains excess columns. Try change your answer"
                                )
                            )

                            studentColumnsCount < referenceColumnsCount -> scriptsResult.add(
                                CheckResult(
                                    0,
                                    "${criterion.message}. Result contains missing columns. Try change your answer"
                                )
                            )

                            studentLines.containsAll(referenceLines) -> scriptsResult.add(
                                CheckResult(
                                    0,
                                    "${criterion.message}. Result contains excess rows. Try change your answer"
                                )
                            )

                            else -> scriptsResult.add(CheckResult(0, criterion.message))
                        }
                    }
                }
            }
            val correctResults = scriptsResult.filter { it.score != 0 }
            return when {
                correctResults.size == scriptsResult.size -> CheckResult(criterion.score, criterion.description)
                correctResults.isEmpty() -> CheckResult(0, scriptsResult.toUserMessage())
                else -> CheckResult(
                    criterion.score / scriptsResult.size * correctResults.size,
                    "Some checks were not successful: ${criterion.message}"
                )
            }
        }

        private fun MutableList<CheckResult>.toUserMessage() =
            this.distinctBy { it.message }
                .joinToString("\n") { it.message }
    }
}

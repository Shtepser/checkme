package checkme.service

import checkme.config.CheckDatabaseConfig
import checkme.domain.checks.MINUTE_TIMEOUT
import checkme.domain.models.User
import checkme.logging.LoggerType
import checkme.logging.ServerLogger
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.UUID
import java.util.concurrent.TimeUnit

const val QUERY_TIMEOUT = 7
// Сервис для работы с временными базами данных, которые создаются для каждой проверки задания с SQL-запросами

@Suppress("TooManyFunctions")
class SqlCheckService(
    private val config: CheckDatabaseConfig,
    private val user: User,
    private val overall: Boolean,
) {
    @Suppress("TooGenericExceptionCaught", "ReturnCount")
    fun getSqlResults(
        firstScript: File,
        referenceQuery: String,
        studentQuery: String,
        checkId: UUID,
    ): Result4k<Pair<String, String>, Pair<SqlCheckError, String>> {
        val studentUser = "student_${checkId}${user.id}"
        val studentPass = "student_pass_${checkId}${user.id}"
        val uniqueDatabaseName = "check${checkId}_${firstScript.name}"
        try {
            createTempDatabase(uniqueDatabaseName)
            createDatabaseUser(
                userName = studentUser,
                pass = studentPass,
                databaseName = uniqueDatabaseName
            )
            executeFirstScript(
                script = firstScript,
                databaseName = uniqueDatabaseName,
                userName = studentUser,
                pass = studentPass
            )
            val studentResult = getQueryResult(
                query = studentQuery,
                databaseName = uniqueDatabaseName,
                user = studentUser,
                pass = studentPass
            )
            val referenceResult = getQueryResult(
                query = referenceQuery,
                databaseName = uniqueDatabaseName,
                user = studentUser,
                pass = studentPass
            )
            return Success(Pair(studentResult, referenceResult))
        } catch (e: SQLException) {
            return Failure(Pair(SqlCheckError.SQL_ERROR, e.message ?: "Unknown SQL error"))
        } catch (e: Exception) {
            ServerLogger.log(
                user = user,
                action = "Check database warnings",
                message = "Error: ${e.message} when try to check student answer. Ask for help",
                type = LoggerType.WARN
            )
            return Failure(Pair(SqlCheckError.SERVER_ERROR, e.message ?: "Unknown server error"))
        } finally {
            dropDatabaseAndUserForCheck(
                name = uniqueDatabaseName,
                user = studentUser
            )
            if (overall) {
                ServerLogger.log(
                    user = user,
                    action = "Check database actions",
                    message = "Database for check dropped",
                    type = LoggerType.INFO
                )
            }
        }
    }

    private fun createDatabaseUser(
        userName: String,
        pass: String,
        databaseName: String,
    ) {
        val connection = createRootConnection()
        connection.use {
            val statement = it.createStatement()
            statement.execute("CREATE USER '$userName'@'%' IDENTIFIED BY '$pass'")
            statement.execute("GRANT ALL PRIVILEGES ON `$databaseName`.* TO '$userName'@'%'")
            statement.execute("FLUSH PRIVILEGES")
            if (overall) {
                ServerLogger.log(
                    user = user,
                    action = "Check database actions",
                    message = "User $userName created with access to database $databaseName",
                    type = LoggerType.INFO
                )
            }
        }
    }

    private fun createTempDatabase(name: String) {
        val connection = createRootConnection()
        connection.use {
            it.createStatement().execute(
                "CREATE DATABASE `$name`;"
            )
            if (overall) {
                ServerLogger.log(
                    user = user,
                    action = "Check database actions",
                    message = "Database $name created",
                    type = LoggerType.INFO
                )
            }
        }
    }

    private fun executeFirstScript(
        script: File,
        databaseName: String,
        userName: String,
        pass: String,
    ) {
        val process = ProcessBuilder(
            "mysql",
            "-u",
            userName,
            "-p$pass",
            "-h",
            "localhost",
            "-P",
            "3306",
            "-D",
            databaseName,
            "-e",
            "source ${script.absolutePath}"
        ).start()
        if (!process.waitFor(MINUTE_TIMEOUT.toLong(), TimeUnit.SECONDS)) {
            ServerLogger.log(
                user = user,
                action = "First script execute",
                message = "Error: The time for the process has expired",
                type = LoggerType.WARN
            )
            process.destroy()
        }
        val exitCode = process.exitValue()
        if (exitCode != 0) {
            val error = process.errorStream.bufferedReader().readText()
            ServerLogger.log(
                user = user,
                action = "First script execute",
                message = "MySQL execution failed: $exitCode: $error",
                type = LoggerType.WARN
            )
        }
    }

    private fun getQueryResult(
        query: String,
        databaseName: String,
        user: String,
        pass: String,
    ): String {
        val cleanedQuery = cleanQuery(query)
        val connection = createDatabaseConnection(
            name = databaseName,
            user = user,
            pass = pass
        )
        connection.autoCommit = false
        connection.use {
            val statement = it.createStatement()
            statement.queryTimeout = QUERY_TIMEOUT
            val isResultSet = statement.execute(cleanedQuery)

            return when {
                isResultSet -> {
                    val result = statement.resultSet.convertToString()
                    connection.rollback()
                    result
                }

                else -> getChangesInTables(
                    statement = statement,
                    connection = connection
                )
            }
        }
    }

    private fun getChangesInTables(
        statement: Statement,
        connection: Connection,
    ): String {
        val allTablesList = statement.executeQuery("SHOW TABLES;").getAllTablesNames().sorted()
        val result = mutableListOf<String>()
        for (table in allTablesList) {
            val tableResult = statement.executeQuery("SELECT * FROM `$table`;")
            result.add(tableResult.convertToString())
        }
        connection.rollback()
        changeAutoIncrementValues(
            allTablesList = allTablesList,
            statement = statement
        )
        return result.joinToString("||")
    }

    private fun changeAutoIncrementValues(
        allTablesList: List<String>,
        statement: Statement,
    ) {
        for (table in allTablesList) {
            val autoIncrementColumns = statement.executeQuery(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() " +
                    "AND TABLE_NAME = '$table'" +
                    "AND EXTRA LIKE '%auto_increment%';"
            )
            while (autoIncrementColumns.next()) {
                val autoIncrementColumn = autoIncrementColumns.getString("COLUMN_NAME")
                val maxIdResult = statement.executeQuery(
                    "SELECT COALESCE(MAX(`$autoIncrementColumn`), 0) as max_id FROM `$table`;"
                )
                if (maxIdResult.next()) {
                    setAutoIncrementValue(
                        maxIdResult = maxIdResult,
                        table = table,
                        statement = statement
                    )
                }
            }
        }
    }

    private fun setAutoIncrementValue(
        maxIdResult: ResultSet,
        table: String,
        statement: Statement,
    ) {
        val maxId = maxIdResult.getLong(1)
        val newAutoIncrementValue = maxId + 1
        statement.executeUpdate(
            "ALTER TABLE `$table` AUTO_INCREMENT = $newAutoIncrementValue;"
        )
        if (overall) {
            ServerLogger.log(
                user = user,
                action = "Check database actions",
                message = "В таблице $table AUTO_INCREMENT установлен в $newAutoIncrementValue",
                type = LoggerType.INFO
            )
        }
    }

    private fun createRootConnection(): Connection =
        DriverManager
            .getConnection(config.jdbc, config.user, config.password)

    private fun createDatabaseConnection(
        name: String,
        user: String,
        pass: String,
    ): Connection = DriverManager.getConnection("${config.jdbc}/$name", user, pass)

    private fun dropDatabaseAndUserForCheck(
        name: String,
        user: String,
    ) {
        val connection = createRootConnection()
        connection.use {
            it.createStatement().execute("DROP DATABASE IF EXISTS `$name`;")
            it.createStatement().execute("DROP USER IF EXISTS '$user'@'%';")
        }
    }

    private fun cleanQuery(query: String): String {
        return query.split(";").filterNot { line ->
            line.trim().startsWith("USE", ignoreCase = true)
        }.joinToString("\n")
    }
}

enum class SqlCheckError(val errorText: String) {
    SQL_ERROR("Solution contains sql error."),
    SERVER_ERROR("Something was wrong while the SQL task was checked. Please ask for help."),
}

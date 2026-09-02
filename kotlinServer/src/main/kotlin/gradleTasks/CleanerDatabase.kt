package gradleTasks

import checkme.config.AppConfig
import checkme.db.generated.enums.UserRole
import checkme.db.generated.tables.references.USERS
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.sql.DriverManager

fun main() {
    val config = AppConfig.fromEnvironment()
    val db = config.databaseConfig
    try {
        DriverManager.getConnection(db.jdbc, db.user, db.password).use { connection ->
            val dsl = DSL.using(connection, SQLDialect.POSTGRES)
            val deletedRows = dsl.deleteFrom(USERS)
                .where(USERS.ROLE.eq(UserRole.STUDENT))
                .execute()
            println("Deleted users: $deletedRows")
        }
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}

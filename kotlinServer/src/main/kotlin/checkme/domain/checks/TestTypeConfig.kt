package checkme.domain.checks

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SqlCheckTest::class, name = "sql-check"),
    JsonSubTypes.Type(value = ConsoleCheckTest::class, name = "console-check")
)
sealed class TestConfig

data class SqlCheckTest(
    val type: String,
    val dbScript: String,
    val referenceQuery: String
) : TestConfig()

data class ConsoleCheckTest(
    val type: String,
    val command: String,
    val expected: String
) : TestConfig()
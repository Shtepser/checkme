package ru.yarsu.serializableClasses.task

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator("type")
sealed class TestConfig {
    @Serializable
    @SerialName("sql-check")
    data class SqlCheck(
        val dbScript: String,
        val referenceQuery: String
    ) : TestConfig()

    @Serializable
    @SerialName("console-check")
    data class ConsoleCheck(
        val command: String,
        val expected: String
    ) : TestConfig()
}
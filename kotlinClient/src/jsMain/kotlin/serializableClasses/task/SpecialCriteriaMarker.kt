package ru.yarsu.serializableClasses.task

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class SpecialCriteriaMarker {
    @SerialName("null")
    NULL_MARKER,
    @SerialName("beforeEach")
    BEFORE_EACH,
    @SerialName("beforeAll")
    BEFORE_ALL,
    @SerialName("afterEach")
    AFTER_EACH,
    @SerialName("afterAll")
    AFTER_ALL
}
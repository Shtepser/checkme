package ru.yarsu.serializableClasses.task

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Criterion(
    val description: String,
    val score: Int,
    val test: TestConfig,
    val message: String,
    @SerialName("special_marker")
    val specialMarker: SpecialCriteriaMarker? = null
)
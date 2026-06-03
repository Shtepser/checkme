package ru.yarsu.serializableClasses.task

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class SpecialCriteriaMarker {
    NULL,
    BEFORE_EACH,
    BEFORE_ALL,
    AFTER_EACH,
    AFTER_ALL
}
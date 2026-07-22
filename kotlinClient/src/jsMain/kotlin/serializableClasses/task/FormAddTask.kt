package ru.yarsu.serializableClasses.task

import kotlinx.serialization.Serializable
import io.kvision.types.KFile

@Serializable
data class FormAddTask(
    val name: String,
    val description: String,
    val criterion: String,
    val format: String,
    val script: List<KFile>? = null,
    val files: List<KFile>? = null
)

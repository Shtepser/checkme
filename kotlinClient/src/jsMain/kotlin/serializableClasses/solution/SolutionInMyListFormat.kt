package ru.yarsu.serializableClasses.solution

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class TaskWithBestResult(
    val taskId: Uuid,
    val taskName: String,
    val highestScore: Int,
    val bestSolution: Int
)

@Serializable
data class SolutionInMyListFormat(
    val bundleName: String,
    val taskWithBestResult: List<TaskWithBestResult>
)

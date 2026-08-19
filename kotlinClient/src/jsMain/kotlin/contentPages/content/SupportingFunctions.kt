package ru.yarsu.contentPages.content

import io.kvision.rest.HttpMethod
import org.w3c.fetch.RequestInit
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.solution.ResultScoreMessage

internal fun createRequestHeaders(
    httpMethod : HttpMethod,
) : RequestInit {
    val requestInit = RequestInit()
    requestInit.method = httpMethod.name
    requestInit.headers = js("{}")
    requestInit.headers["Authentication"] = "Bearer ${UserInformationStorage.getUserInformation()?.token}"
    return requestInit
}

fun getSolutionBlockColorName(result: Map<String, ResultScoreMessage>?): String {
    val score = result?.map { it.value.score }
    return if (score == null) {
        Result.ERROR.cssName
    } else if (score.sum() == 0) {
        Result.INCORRECT.cssName
    } else if (score.contains(0)) {
        Result.PARTIAL.cssName
    } else {
        Result.CORRECT.cssName
    }
}

fun getTaskBlockColorName(score: Int, result: Int): Result {
    return if ((result == -2) && (score == -2)) {
        Result.ERROR
    } else if (result < 0) {
        Result.NO
    } else if (result == 0) {
        Result.INCORRECT
    } else if (result < score) {
        Result.PARTIAL
    } else {
        Result.CORRECT
    }
}

enum class Result(val message: String, val cssName: String) {
    ERROR("", "error"),
    NO("Нет решений", "no"),
    INCORRECT("Нажмите, чтобы посмотреть все отправленные решения", "incorrect"),
    PARTIAL("Нажмите, чтобы посмотреть все отправленные решения", "partial"),
    CORRECT("Нажмите, чтобы посмотреть все отправленные решения", "correct"),
}
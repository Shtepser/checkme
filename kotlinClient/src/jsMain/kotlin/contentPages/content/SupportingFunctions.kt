package ru.yarsu.contentPages.content

import io.kvision.core.onChange
import io.kvision.form.text.text
import io.kvision.html.ButtonStyle
import io.kvision.html.InputType
import io.kvision.html.button
import io.kvision.panel.HPanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
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

fun paginationBlock(routing: Routing, path: String, page: Int, positionTop: Boolean = true) : HPanel {
    return HPanel(className = if (positionTop) "pagination-top" else "pagination-bottom") {
        button("Назад", style = ButtonStyle.LINK).onClick {
            routing.navigate("$path${page - 1}")
        }
        text(InputType.NUMBER,"$page") {
            input.addCssClass("page-input")
            addCssClass("page")
        }.onChange {
            routing.navigate("$path${this.value}")
        }
        button("Вперёд", style = ButtonStyle.LINK).onClick {
            routing.navigate("$path${page + 1}")
        }
    }
}
package ru.yarsu.contentPages.content.mySolutionListPage

import io.kvision.html.Div
import io.kvision.html.h2
import io.kvision.panel.SimplePanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import ru.yarsu.contentPages.content.createRequestHeaders
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.solution.TaskOrUserSolutionsFormat
import kotlin.uuid.Uuid

class MySolutionList(
    private val page: Int?,
    taskId: Uuid?,
    serverUrl: String,
    private val routing: Routing
) : SimplePanel() {
    init {
        val requestInit = createRequestHeaders(HttpMethod.GET)
        window.fetch(serverUrl + "solution/user-and-task/${taskId.toString()}", requestInit).then { response ->
            when (response.status.toInt()) {
                200 -> response.json().then {
                    val jsonString = JSON.stringify(it)
                    val taskSolutions = Json.decodeFromString<TaskOrUserSolutionsFormat>(jsonString)
                    h2("Ваши решения задачи: ${taskSolutions.name}")
                    if (taskSolutions.solutions.isEmpty()) {
                        this.add(Div("Решения не найдены"))
                    } else {
                        this.add(MySolutionListViewer(taskSolutions.solutions, routing))
                    }
                }

                400 -> response.json().then {
                    val jsonString = JSON.stringify(it)
                    val responseError =
                        Json.Default.decodeFromString<ResponseError>(jsonString)
                    this.add(Div(responseError.error, className = "error-message"))
                }

                else -> this.add(
                    Div(
                        "Код ошибки ${response.status}: ${response.statusText}",
                        className = "error-message"
                    )
                )
            }
        }
    }
}

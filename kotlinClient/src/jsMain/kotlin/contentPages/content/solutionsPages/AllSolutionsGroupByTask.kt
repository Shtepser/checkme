package ru.yarsu.contentPages.content.solutionsPages

import io.kvision.html.ButtonStyle
import io.kvision.html.Div
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.h2
import io.kvision.panel.SimplePanel
import io.kvision.panel.hPanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import ru.yarsu.contentPages.content.createRequestHeaders
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.solution.SolutionsGroupByTask

class AllSolutionsGroupByTask(
    private val page: Int?,
    serverUrl: String,
    private val routing: Routing
) : SimplePanel() {
    init {
        h2("Все решения")
        if ((page == null) || (page < 1)) {
            routing.navigate("/task-solutions-list/1")
        } else {
            hPanel(className = "pagination-top") {
                button("Назад", style = ButtonStyle.LINK).onClick {
                    routing.navigate("/task-solutions-list/${page - 1}")
                }
                div("$page", className = "page")
                button("Вперёд", style = ButtonStyle.LINK).onClick {
                    routing.navigate("/task-solutions-list/${page + 1}")
                }
            }
            hPanel(className = "pagination-bottom") {
                button("Назад", style = ButtonStyle.LINK).onClick {
                    routing.navigate("/task-solutions-list/${page - 1}")
                }
                div("$page", className = "page")
                button("Вперёд", style = ButtonStyle.LINK).onClick {
                    routing.navigate("/task-solutions-list/${page + 1}")
                }
            }
            val requestInit = createRequestHeaders(HttpMethod.GET)
            window.fetch(serverUrl + "solution/tasks-solutions/$page", requestInit).then { response ->
                when (response.status.toInt()) {
                    200 -> response.json().then {
                        val jsonString = JSON.stringify(it)
                        if (UserInformationStorage.isAdmin()) {
                            val solutionList = Json.Default.decodeFromString<List<SolutionsGroupByTask>>(jsonString)
                            if (solutionList.isEmpty()) {
                                this.add(Div("Решения не найдены", className = "not-found"))
                            } else {
                                this.add(AllSolutionsGroupByTaskViewer(solutionList, routing))
                            }
                        }
                    }

                    400 -> response.json().then {
                        val jsonString = JSON.stringify(it)
                        val responseError = Json.Default.decodeFromString<ResponseError>(jsonString)
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
}
package ru.yarsu.contentPages.content.mySolutionListPage

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
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.solution.SolutionInMyListFormat

class MyResultList(
    private val page: Int?,
    serverUrl: String,
    private val routing: Routing
) : SimplePanel() {
    init {
        h2("Мои решения")
        if ((page == null) || (page < 1)) {
            routing.navigate("/my-result-list/1")
        } else {
            hPanel(className = "pagination") {
                button("Назад").onClick {
                    routing.navigate("/my-result-list/${page - 1}")
                }
                div("Страница $page")
                button("Вперёд").onClick {
                    routing.navigate("/my-result-list/${page + 1}")
                }
            }
            val requestInit = createRequestHeaders(HttpMethod.GET)
            window.fetch(serverUrl + "solution/me/$page", requestInit).then { response ->
                when (response.status.toInt()) {
                    200 -> response.json().then {
                        val jsonString = JSON.stringify(it)
                        console.log(jsonString)
                        val solutionList = Json.decodeFromString<List<SolutionInMyListFormat>>(jsonString)
                        if (solutionList.isEmpty()) {
                            this.add(Div("Решения не найдены"))
                        } else {
                            this.add(MyResultListViewer(solutionList, routing))
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
}
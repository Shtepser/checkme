package ru.yarsu.contentPages.content.solutionsPages

import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.html.Div
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.h2
import io.kvision.panel.SimplePanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import ru.yarsu.contentPages.content.createRequestHeaders
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.solution.SolutionsTable

class AllSolutionsTable(
    serverUrl: String,
    private val routing: Routing
) : SimplePanel(className = "paged-layout") {
    init {
        h2("Все решения")
        val downloadButton = Button("Скачать", style = ButtonStyle.PRIMARY)
        div(className = "page-actions") {
            add(downloadButton)
            button("Список", style = ButtonStyle.LINK).onClick {
                routing.navigate("/solution-list/1")
            }
        }
        val requestInit = createRequestHeaders(HttpMethod.GET)
        window.fetch(serverUrl + "solution/solutions_table", requestInit).then { response ->
            when (response.status.toInt()) {
                200 -> response.json().then {
                    val jsonString = JSON.stringify(it)
                    val solutions = Json.Default.decodeFromString<SolutionsTable>(jsonString)
                    this.add(AllSolutionsTableViewer(routing, solutions, downloadButton))
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
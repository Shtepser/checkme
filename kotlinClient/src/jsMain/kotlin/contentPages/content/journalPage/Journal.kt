package ru.yarsu.contentPages.content.journalPage

import io.kvision.html.*
import io.kvision.panel.SimplePanel
import io.kvision.panel.hPanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import ru.yarsu.contentPages.content.createRequestHeaders
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.logger.LogFileInfo

class Journal(
    private val page: Int?,
    private val serverUrl: String,
    private val routing: Routing
) : SimplePanel(className = "paged-layout") {
    init {
        h2("Журнал действий")
        if ((page == null) || (page < 1)) {
            routing.navigate("/journal/1")
        } else {
            hPanel(className = "pagination-top") {
                button("Назад", style = ButtonStyle.LINK).onClick {
                    routing.navigate("/journal/${page - 1}")
                }
                div("$page", className = "page")
                button("Вперёд", style = ButtonStyle.LINK).onClick {
                    routing.navigate("/journal/${page + 1}")
                }
            }
            hPanel(className = "pagination-bottom") {
                button("Назад", style = ButtonStyle.LINK).onClick {
                    routing.navigate("/journal/${page - 1}")
                }
                div("$page", className = "page")
                button("Вперёд", style = ButtonStyle.LINK).onClick {
                    routing.navigate("/journal/${page + 1}")
                }
            }
            loadLogsFilesList(page)
        }
    }

    private fun loadLogsFilesList(
        page: Int?
    ) {
        val requestInit = createRequestHeaders(HttpMethod.GET)
        window.fetch(serverUrl + "admin/journal/$page", requestInit).then { response ->
            when (response.status.toInt()) {
                200 -> response.json().then {
                    val jsonString = JSON.stringify(it)
                    val logFiles = Json.decodeFromString<List<LogFileInfo>>(jsonString)
                    if (logFiles.isEmpty()) {
                        this.add(Div("Нет данных для отображения", className = "not-found"))
                    } else {
                        this.add(JournalViewer(routing, logFiles))
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
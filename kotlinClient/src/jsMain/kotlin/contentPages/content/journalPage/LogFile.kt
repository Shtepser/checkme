package ru.yarsu.contentPages.content.journalPage

import io.kvision.html.ButtonStyle
import io.kvision.html.Div
import io.kvision.html.button
import io.kvision.html.h2
import io.kvision.panel.SimplePanel
import io.kvision.panel.hPanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import ru.yarsu.contentPages.content.createRequestHeaders
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.logger.LogFormat

class LogFile(
    private val serverUrl: String,
    private val routing: Routing,
    private val name: String?
) : SimplePanel() {
    init {
        h2("Файл $name") {
            if (name == null) {
                routing.navigate("/journal/1")
            } else {
                loadLogsFromFile(name)
            }
        }
        hPanel {
            button("Назад к списку файлов", style = ButtonStyle.LINK).onClick {
                routing.navigate("/journal/1")
            }
            button("Обновить", style = ButtonStyle.SECONDARY).onClick {
                js("window.location.reload()")
            }
        }
    }

    private fun loadLogsFromFile(
        fileName: String,
    ) {
        val requestInit = createRequestHeaders(HttpMethod.GET)
        window.fetch(serverUrl + "admin/journal/file/$fileName", requestInit).then { response ->
            when (response.status.toInt()) {
                200 -> response.json().then {
                    val jsonString = JSON.stringify(it)
                    val logs = Json.decodeFromString<List<LogFormat>>(jsonString)
                    if (logs.isEmpty()) {
                        this.add(Div("Нет данных для отображения", className = "not-found"))
                    } else {
                        this.add(LogFileViewer(logs))
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
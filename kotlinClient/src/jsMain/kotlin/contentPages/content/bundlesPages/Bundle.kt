package ru.yarsu.contentPages.content.bundlesPages

import io.kvision.html.Div
import io.kvision.panel.SimplePanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import ru.yarsu.contentPages.content.createRequestHeaders
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.bundle.BundleFormatWithTasks
import kotlin.uuid.Uuid

class Bundle(
    bundleId: Uuid?,
    serverUrl: String,
    private val routing: Routing
) : SimplePanel() {
    init {
        val requestInit = createRequestHeaders(HttpMethod.GET)
        window.fetch(serverUrl + "bundle/$bundleId", requestInit).then { response ->
            when (response.status.toInt()) {
                200 -> response.json().then {
                    val jsonString = JSON.stringify(it)
                    val bundleAndTasks = Json.Default.decodeFromString<BundleFormatWithTasks>(jsonString)
                    this.add(BundleViewer(serverUrl, bundleAndTasks.bundle, bundleAndTasks.tasks, routing))
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
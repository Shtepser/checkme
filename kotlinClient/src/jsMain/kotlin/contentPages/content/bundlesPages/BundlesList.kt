package ru.yarsu.contentPages.content.bundlesPages

import io.kvision.html.ButtonStyle
import io.kvision.html.Div
import io.kvision.html.button
import io.kvision.html.h2
import io.kvision.panel.SimplePanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import ru.yarsu.contentPages.content.addBundlePage.AddBundle
import ru.yarsu.contentPages.content.createRequestHeaders
import ru.yarsu.enumClasses.ListType
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.bundle.BundleFormat

class BundlesList(
    serverUrl: String,
    private val routing: Routing,
    listType: ListType
) : SimplePanel() {
    init {
        if (listType.ordinal == 0) {
            h2("Наборы заданий")
        } else {
            h2("Скрытые наборы заданий")
        }
        val addBundle = AddBundle(serverUrl, routing)
        if (UserInformationStorage.isAdmin()) {
            button(
                "Cоздать набор",
                style = ButtonStyle.LINK
            ).onClick { addBundle.show() }
        }
        val requestInit = createRequestHeaders(HttpMethod.GET)
        window.fetch(serverUrl + "bundle/${listType.keyWord}", requestInit).then { response ->
            when (response.status.toInt()) {
                200 -> response.json().then {
                    val jsonString = JSON.stringify(it)
                    val bundleList = Json.decodeFromString<List<BundleFormat>>(jsonString)
                    if (bundleList.isEmpty()) {
                        this.add(Div("Наборы не найдены", className = "not-found"))
                    } else {
                        this.add(BundleListViewer(serverUrl, routing, bundleList))
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
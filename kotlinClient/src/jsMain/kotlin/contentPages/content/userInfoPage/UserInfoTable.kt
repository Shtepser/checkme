package ru.yarsu.contentPages.content.userInfoPage

import io.kvision.html.Div
import io.kvision.html.h2
import io.kvision.panel.SimplePanel
import io.kvision.rest.HttpMethod
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import ru.yarsu.contentPages.content.createRequestHeaders
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.admin.UserFullData

class UserInfoTable(
    serverUrl: String,
) : SimplePanel(className = "page-head") {
    init {
        h2("Информация о пользователях")
        val requestInit = createRequestHeaders(HttpMethod.GET)
        window.fetch(serverUrl + "admin/get-users-info", requestInit).then { response ->
            when (response.status.toInt()) {
                200 -> {
                    response.json().then {
                        val jsonString = JSON.stringify(it)
                        val userList = Json.decodeFromString<List<UserFullData>>(jsonString)
                        this.add(UserInfoTableViewer(serverUrl, userList))
                    }
                }

                400 -> {
                    response.json().then {
                        val jsonString = JSON.stringify(it)
                        val responseError =
                            Json.Default.decodeFromString<ResponseError>(jsonString)
                        this.add(Div(responseError.error, className = "error-message"))
                    }
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
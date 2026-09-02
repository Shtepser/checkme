package ru.yarsu.contentPages.content.userListPage

import io.kvision.html.ButtonStyle
import io.kvision.html.Div
import io.kvision.html.button
import io.kvision.html.h2
import io.kvision.panel.SimplePanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import ru.yarsu.contentPages.content.createRequestHeaders
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.user.UserInList

class UserList(
    serverUrl: String,
    routing: Routing
) : SimplePanel(className = "page-head") {
    init {
        h2("Список пользователей")
        button("Автоматическая регистрация", style = ButtonStyle.LINK).onClick {
            routing.navigate("/automatic-registration")
        }
        fetchAllStudents(
            serverUrl = serverUrl,
            routing = routing
        )
    }

    private fun fetchAllStudents(
        serverUrl: String,
        routing: Routing
    ) {
        val requestInit = createRequestHeaders(HttpMethod.GET)
        window.fetch(serverUrl + "user/all", requestInit).then { response ->
            when (response.status.toInt()) {
                200 -> response.json().then {
                    val jsonString = JSON.stringify(it)
                    val userList = Json.decodeFromString<List<UserInList>>(jsonString)
                    this.add(UserListViewer(userList, routing))
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
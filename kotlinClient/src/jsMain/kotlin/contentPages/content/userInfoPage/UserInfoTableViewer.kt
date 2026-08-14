package ru.yarsu.contentPages.content.userInfoPage

import io.kvision.html.Div
import io.kvision.panel.SimplePanel
import io.kvision.tabulator.ColumnDefinition
import io.kvision.tabulator.Editor
import io.kvision.tabulator.Formatter
import io.kvision.tabulator.TableType
import io.kvision.tabulator.TabulatorOptions
import io.kvision.tabulator.tabulator
import ru.yarsu.serializableClasses.admin.UserFullData
import io.kvision.rest.HttpMethod
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import ru.yarsu.serializableClasses.ResponseError
import kotlin.uuid.Uuid
import ru.yarsu.contentPages.content.createRequestHeaders

class UserInfoTableViewer(
    private val serverUrl: String,
    private val usersData: List<UserFullData>
) : SimplePanel() {
    private val columns = listOf<ColumnDefinition<Map<String, String>>>(
        ColumnDefinition(
            "Логин",
            field = "login",
            headerFilter = Editor.INPUT
        ),
        ColumnDefinition(
            "Фамилия",
            field = "surname",
            headerFilter = Editor.INPUT
        ),
        ColumnDefinition(
            "Имя",
            field = "name",
            headerFilter = Editor.INPUT
        ),
        ColumnDefinition(
            "Системный пароль",
            field = "system-pass"
        ),
        ColumnDefinition(
            "Пароль",
            width = "200",
            field = "password",
            headerFilter = Editor.LIST,
            headerFilterParams = { cell: dynamic ->
                js("{ values: ['', 'Пользовательский', 'Системный'] }")
            }
        )
    )

    init {
        tabulator(
            data = getData(),
            options = TabulatorOptions(
                columns = columns +
                        ColumnDefinition(
                            "Действия",
                            field = "action",
                            formatter = Formatter.HTML,
                            cellClick = { event, _ ->
                                val button = event.asDynamic().target
                                if (button && !button.disabled) {
                                    val userId = button.getAttribute("user-id")
                                    if (userId) changePassword(Uuid.parse(userId))
                                }
                            }
                        )
            ),
            types = setOf(TableType.BORDERED)
        )
    }

    private fun getData(): List<Map<String, String>> {
        return usersData.map { user ->
            mapOf(
                "id" to user.id.toString(),
                "login" to user.login,
                "surname" to user.surname,
                "name" to user.name,
                "password" to if (user.isSystemPass) "Системный" else "Пользовательский",
                "system-pass" to user.systemPass,
                "action" to if (user.isSystemPass) "<button class='btn btn-primary' user-id='${user.id}' " +
                        "disabled>Установить системный пароль</button>" else
                    "<button class='btn btn-primary' user-id='${user.id}'>Установить системный пароль</button>"
            )
        }
    }

    private fun changePassword(userId: Uuid) {
        val requestInit = createRequestHeaders(HttpMethod.POST)
        window.fetch(serverUrl + "admin/set-system-password/$userId", requestInit).then { response ->
            when (response.status.toInt()) {
                200 -> js("window.location.reload()")
                400 -> {
                    response.json().then {
                        val jsonString = JSON.stringify(it)
                        val responseError = Json.Default.decodeFromString<ResponseError>(jsonString)
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
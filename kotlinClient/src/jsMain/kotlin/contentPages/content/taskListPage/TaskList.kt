package ru.yarsu.contentPages.content.taskListPage

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
import ru.yarsu.enumClasses.ListType
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.task.TaskWithBundlesForList

class TaskList(
    serverUrl: String,
    private val routing: Routing,
    listType: ListType
) : SimplePanel(className = "page-head") {
    init {
        if (listType.ordinal == 0) {
            h2("Список задач") { id = "task-list-div"}
        } else {
            h2("Список скрытых задач") { id = "hidden-task-list-div"}
        }
        if (UserInformationStorage.isAdmin()) {
            button(
                "Cоздать задачу",
                style = ButtonStyle.LINK,
            ) { id = "create-task-button" } .onClick { routing.navigate("/add-task") }
        }
        val requestInit = createRequestHeaders(HttpMethod.GET)
        window.fetch(serverUrl + "task/${listType.keyWord}", requestInit).then { response ->
            when (response.status.toInt()) {
                200 -> response.json().then {
                    val jsonString = JSON.stringify(it)
                    val taskList = Json.decodeFromString<List<TaskWithBundlesForList>>(jsonString)
                    if (taskList.isEmpty()){
                        this.add(Div("Задачи не найдены", className = "not-found") { id = "no-task-div"})
                    } else {
                        this.add(TaskListViewer(serverUrl, routing, taskList))
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
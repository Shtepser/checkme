package ru.yarsu.contentPages.content.addBundlePage

import io.kvision.core.Container
import io.kvision.html.ButtonStyle
import io.kvision.html.Div
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.h2
import io.kvision.panel.SimplePanel
import io.kvision.panel.VPanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import ru.yarsu.contentPages.content.createRequestHeaders
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.bundle.TaskFormatWithOrder

class ChangeBundleTasksOrder(
    bundleId: String,
    serverUrl: String,
    private val routing: Routing,
) : SimplePanel(className = "task-selection-page") {
    init {
        h2("Укажите порядок заданий в наборе")
        val requestInit = createRequestHeaders(HttpMethod.GET)
        window.fetch(serverUrl + "bundle/tasks/$bundleId", requestInit).then { response ->
            if (response.status.toInt() == 200) {
                response.text().then { jsonString ->
                    val taskList = Json.decodeFromString<List<TaskFormatWithOrder>>(jsonString)
                    if (taskList.isEmpty()) {
                        this.add(Div("Задачи не найдены"))
                    } else {
                        val tasksContainer = Div(className = "task-selection-container")
                        add(tasksContainer)
                        val saveButton = button(
                            "Сохранить",
                            style = ButtonStyle.PRIMARY
                        ) { id = "save-task-order-button" }
                            .apply {
                                onClick {
                                    val indices: List<String> =
                                        js("getTasksInOrder()").unsafeCast<Array<String>>().toList()
                                    val orderedTasks = indices.mapNotNull { index ->
                                        taskList.find { it.task.id.toString() == index }?.let { taskWithOrder ->
                                            TaskFormatWithOrder(
                                                task = taskWithOrder.task,
                                                order = indices.indexOf(index) + 1
                                            )
                                        }
                                    }
                                    val requestInit = createRequestHeaders(HttpMethod.POST)
                                    requestInit.headers["Content-Type"] = "application/json"
                                    requestInit.body = Json.Default.encodeToString(
                                        orderedTasks
                                    )
                                    window.fetch(serverUrl + "bundle/select-order/$bundleId", requestInit)
                                        .then { response ->
                                            when (response.status.toInt()) {
                                                200 -> response.json().then {
                                                    routing.navigate("bundle/$bundleId")
                                                }

                                                401 -> response.json().then {
                                                    val jsonString = JSON.stringify(it)
                                                    val responseUnauthorized =
                                                        Json.Default.decodeFromString<ResponseError>(jsonString)
                                                    Toast.danger(
                                                        responseUnauthorized.error,
                                                        ToastOptions(
                                                            duration = 3000,
                                                            position = ToastPosition.TOPRIGHT,
                                                        )
                                                    )
                                                }

                                                else -> Toast.danger(
                                                    "Код ошибки ${response.status}: ${response.statusText}",
                                                    ToastOptions(
                                                        duration = 5000,
                                                        position = ToastPosition.TOPRIGHT,
                                                    )
                                                )
                                            }
                                        }
                                }
                            }
                        add(saveButton)

                        taskList.sortedBy { it.order }.forEach { task ->
                            createTaskElement(task, tasksContainer)
                        }
                        js("initDragAndDrop()")
                    }
                }
            } else if (response.status.toInt() == 400) {
                response.json().then {
                    val jsonString = JSON.stringify(it)
                    val responseError =
                        Json.Default.decodeFromString<ResponseError>(jsonString)
                    this.add(Div(responseError.error, className = "error-message"))
                }
            } else {
                this.add(Div("Код ошибки ${response.status}: ${response.statusText}", className = "error-message"))
            }
        }
    }

    private fun createTaskElement(
        taskAndOrder: TaskFormatWithOrder,
        container: Container
    ) {
        val taskItem = VPanel(className = "task-item") {
            id = taskAndOrder.task.id.toString()
            div(taskAndOrder.task.id.toString(), className = "id") {
                visible = false
            }
            div(taskAndOrder.task.name, className = "name")
            val description = taskAndOrder.task.description
                .replace("(<([^>]+)>)".toRegex(), "")
            div(
                if (description.length > 50)
                    "" + description.filterIndexed { index, _ -> index <= 50 } + "..."
                else description
            )
        }
        add(taskItem)
        container.add(taskItem)
    }
}

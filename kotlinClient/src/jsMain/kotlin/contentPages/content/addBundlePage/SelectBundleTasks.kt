package ru.yarsu.contentPages.content.addBundlePage

import io.kvision.core.Container
import io.kvision.core.onClick
import io.kvision.form.check.CheckBox
import io.kvision.html.*
import io.kvision.panel.SimplePanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import ru.yarsu.contentPages.content.createRequestHeaders
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.task.TaskFormatForList
import ru.yarsu.serializableClasses.task.TaskWithBundlesForList

const val DESCRIPTION_SIZE = 150

class SelectBundleTasks(
    bundleId: String,
    serverUrl: String,
    private val routing: Routing,
) : SimplePanel(className = "task-selection-page") {
    private val selectedTasks = mutableSetOf<String>()

    init {
        h2("Выбор задач")
        val saveButton = button(
            "Далее",
            style = ButtonStyle.PRIMARY,
        ).apply {
            id = "next-button"
            disabled = true
            onClick {
                if (selectedTasks.isNotEmpty()) {
                    val requestInit = createRequestHeaders(HttpMethod.POST)
                    requestInit.headers["Content-Type"] = "application/json"
                    requestInit.body = Json.Default.encodeToString(
                        selectedTasks
                    )
                    window.fetch(serverUrl + "bundle/select-tasks/$bundleId", requestInit).then { response ->
                        when (response.status.toInt()) {
                            200 -> response.json().then {
                                routing.navigate("bundle/select-order/$bundleId")
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
        }
        add(saveButton)
       selectAllTasks(serverUrl, saveButton)
    }

    private fun selectAllTasks(
       serverUrl: String,
       saveButton: Button
    ) {
        val requestInit = createRequestHeaders(HttpMethod.GET)
        window.fetch(serverUrl + "task/all", requestInit).then { response ->
            if (response.status.toInt() == 200) {
                response.json().then {
                    val jsonString = JSON.stringify(it)
                    val taskListWithBundles = Json.decodeFromString<List<TaskWithBundlesForList>>(jsonString)
                    if (taskListWithBundles.isEmpty()) {
                        this.add(Div("Задачи не найдены"))
                    } else {
                        val tasksContainer = Div(className = "task-selection-container")
                        add(tasksContainer)

                        taskListWithBundles.forEachIndexed  { index, taskAndBundle ->
                            createTaskElement(taskAndBundle.task, tasksContainer, saveButton, index + 1)
                        }
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
        task: TaskFormatForList,
        container: Container,
        nextButton: Button,
        index: Int
    ) {
        val taskDiv = Div(className = "task-item").apply {
            id = "task-item-$index"
            val checkbox =
                CheckBox(
                    value = false,
                    label = "${task.name} (${
                        task.description.replace("(<([^>]+)>)".toRegex(), "")
                            .slice(0..DESCRIPTION_SIZE - task.name.length)
                    }...)"
                )
            add(checkbox)
            onClick {
                checkbox.value = !checkbox.value
                changeTaskSelection(
                    task.id.toString(),
                    checkbox.value,
                    nextButton
                )
            }
        }
        container.add(taskDiv)
    }

    private fun changeTaskSelection(taskId: String, isSelected: Boolean, nextButton: Button) {
        if (isSelected) {
            selectedTasks.add(taskId)
        } else {
            selectedTasks.remove(taskId)
        }
        nextButton.disabled = selectedTasks.isEmpty()
    }
}
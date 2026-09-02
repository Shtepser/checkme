package ru.yarsu.contentPages.content.bundlesPages

import io.kvision.core.onClick
import io.kvision.dropdown.dropDown
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.h2
import io.kvision.html.h3
import io.kvision.html.h4
import io.kvision.panel.VPanel
import io.kvision.panel.hPanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import ru.yarsu.contentPages.content.hiddenBundle.BundleHiddenButton
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import ru.yarsu.contentPages.content.createRequestHeaders
import ru.yarsu.contentPages.content.getTaskBlockColorName
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.bundle.BundleFormat
import ru.yarsu.serializableClasses.bundle.TaskFormatWithOrder

class BundleViewer(
    private val serverUrl: String,
    private val bundle: BundleFormat,
    private val tasksAndOrders: List<TaskFormatWithOrder>,
    private val routing: Routing
) : VPanel(className = "Bundle") {
    init {
        h2(bundle.name)
        if (UserInformationStorage.isAdmin()) {
            dropDown("Действия", style = ButtonStyle.SECONDARY) {
                val editBundle = ChangeBundleName(serverUrl, bundle.id.toString(), routing)
                button("Переименовать", style = ButtonStyle.PRIMARY).onClick {
                    editBundle.show()
                }
                if (tasksAndOrders.isEmpty()) {
                    button("Добавить задачи", style = ButtonStyle.PRIMARY).onClick {
                        routing.navigate("/bundle/select-bundle-tasks/${bundle.id}")
                    }
                } else {
                    button("Изменить состав", style = ButtonStyle.PRIMARY).onClick {
                        routing.navigate("/bundle/select-bundle-tasks/${bundle.id}")
                    }
                    button("Изменить порядок", style = ButtonStyle.PRIMARY).onClick {
                        routing.navigate("/bundle/select-order/${bundle.id}")
                    }
                }
                val hiddenButton = BundleHiddenButton(
                    serverUrl,
                    bundle.isActual,
                    bundle.id,
                )
                this.add(hiddenButton)
                button("Удалить набор", style = ButtonStyle.DANGER).onClick {
                    tryDeleteBundle()
                }
            }
        }
        if (UserInformationStorage.isAdmin()) {
            if (bundle.isActual) h4("Набор актуален")
            else h4("Набор не является актуальным")
        }
        h3("Задачи набора")
        if (tasksAndOrders.isEmpty()) div("Задачи не найдены", className = "not-found")
        for (taskAndOrder in tasksAndOrders) {
            hPanel(className = "bundle-in-list") {
                val highestScore = taskAndOrder.task.highestScore ?: -2
                val bestScore = taskAndOrder.task.bestScore ?: -2
                val taskItem = VPanel(className = "bundle-item ${getTaskBlockColorName(highestScore, bestScore).cssName}") {
                    div(taskAndOrder.task.name, className = "name")
                    val description = taskAndOrder.task.description
                        .replace("(<([^>]+)>)".toRegex(), "")
                    div(
                        if (description.length > 50)
                            "" + description.filterIndexed { index, _ -> index <= 50 } + "..."
                        else description
                    )
                }.apply {
                    this.onClick {
                        routing.navigate("task/${taskAndOrder.task.id}")
                    }
                }
                this.add(taskItem)
            }
        }
    }

    private fun tryDeleteBundle() {
        val requestInit = createRequestHeaders(HttpMethod.DELETE)
        window.fetch(serverUrl + "bundle/delete/${bundle.id}", requestInit).then { response ->
            when (response.status.toInt()) {
                200 -> routing.navigate("/")
                400 -> response.json().then {
                    val jsonString = JSON.stringify(it)
                    val responseError =
                        Json.Default.decodeFromString<ResponseError>(jsonString)
                    Toast.danger(
                        responseError.error,
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
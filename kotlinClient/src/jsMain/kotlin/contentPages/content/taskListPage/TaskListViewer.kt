package ru.yarsu.contentPages.content.taskListPage

import io.kvision.core.onClick
import io.kvision.html.div
import io.kvision.panel.VPanel
import io.kvision.panel.hPanel
import io.kvision.routing.Routing
import ru.yarsu.contentPages.content.hiddenTask.TaskHiddenButton
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.task.TaskWithBundlesForList
import kotlin.math.absoluteValue

class TaskListViewer(
    private val serverUrl: String,
    private val routing: Routing,
    taskList: List<TaskWithBundlesForList>
) : VPanel() {
    init {
        taskList.forEachIndexed { index, taskWithBundles ->
            if (taskWithBundles.task.isActual || UserInformationStorage.isAdmin()) {
                hPanel(className = "task-in-list") {
                    val taskItem = VPanel(className = "task-item") {
                        id = "task-item-${index + 1}"
                        div(className = "task-content") {
                            div(className = "task-text") {
                                div(taskWithBundles.task.name, className = "name")
                                val description = taskWithBundles.task.description
                                    .replace("(<([^>]+)>)".toRegex(), "")
                                div(
                                    if (description.length > 50)
                                        description.filterIndexed { index, _ -> index <= 50 } + "..."
                                    else description
                                )
                            }
                            div(className = "task-groups") {
                                taskWithBundles.bundles.forEach { bundle ->
                                    div(className = "group-element", content = bundle.name) {
                                        setStyle("background-color", bundle.name.toPastelColor())
                                    }
                                }
                            }
                        }
                    }.apply {
                        this.onClick {
                            routing.navigate("task/${taskWithBundles.task.id}")
                        }
                    }
                    this.add(taskItem)
                    if (UserInformationStorage.isAdmin()) {
                        val hiddenButton = TaskHiddenButton(
                            serverUrl,
                            taskWithBundles.task.isActual,
                            taskWithBundles.task.id,
                            this
                        )
                        this.add(hiddenButton)
                    }
                }
            }
        }
    }

    private fun String.toPastelColor(): String {
        val hash = this.hashCode()
        val hue = (hash % 360).absoluteValue
        return "hsl($hue, 40%, 70%)"
    }
}
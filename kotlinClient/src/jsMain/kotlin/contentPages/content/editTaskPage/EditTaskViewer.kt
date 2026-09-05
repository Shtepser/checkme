package ru.yarsu.contentPages.content.editTaskPage

import io.kvision.core.Display
import io.kvision.core.onChangeLaunch
import io.kvision.core.onClickLaunch
import io.kvision.form.check.RadioGroup
import io.kvision.form.formPanel
import io.kvision.form.text.RichText
import io.kvision.form.text.Text
import io.kvision.form.text.TextArea
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.html.Div
import io.kvision.html.Label
import io.kvision.html.button
import io.kvision.html.h2
import io.kvision.panel.VPanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.types.KFile
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.url.URL
import org.w3c.xhr.FormData
import ru.yarsu.contentPages.content.addTaskPage.criterionsUpload
import ru.yarsu.contentPages.content.addTaskPage.fileToSend
import ru.yarsu.contentPages.content.addTaskPage.scriptFilesUpload
import ru.yarsu.contentPages.content.addTaskPage.sendTask
import ru.yarsu.contentPages.content.addTaskPage.validateForm
import ru.yarsu.contentPages.content.createRequestHeaders
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.task.AnswerFormat
import ru.yarsu.serializableClasses.task.FormAddTask
import ru.yarsu.serializableClasses.task.TaskFormat

class EditTaskViewer(
    private val task: TaskFormat,
    private val serverUrl: String,
    private val routing: Routing
) : VPanel(className = "TaskAdd") {
    private val scriptFiles = mutableListOf<KFile>()
    private var editScripts = false
    init {
        val taskType = task.answerFormat.first().type
        h2("Редактирование задачи")
        val formPanelEditTask = formPanel<FormAddTask>(className = "base-form") {
            add(Label("Название", className = "separate-form-label"))
            add(
                FormAddTask::name,
                Text(value = task.name),
            )
            add(Label("Описание", className = "separate-form-label"))
            add(
                FormAddTask::description,
                RichText(value = task.description),
            )
            add(Label("JSON с критериями задачи", className = "separate-form-label"))
            val textArea = TextArea(value = Json.encodeToString(task.criterions))
            add(
                Label("Выберите JSON файл", forId = "input-file-0", className = "btn btn-secondary")
            )
            add(
                criterionsUpload(this@formPanel, textArea)
            )
            add(
                FormAddTask::criterion,
                textArea,
            )
            val buttonEditScripts = Button("Изменить скрипты задачи", style = ButtonStyle.SECONDARY) {
                if (taskType == "text") {
                    display = Display.NONE
                }
            }
            val scriptLabel = Label("Скрипт", className = "separate-form-label") {
                display = Display.NONE
            }
            val inputFileLabel = Label("Выберите файлы", forId = "input-file-1", className = "btn btn-secondary") {
                display = Display.NONE
            }
            val addedScriptsFileViewer = Div("Файлы не выбраны", className = "files-viewer") {
                display = Display.NONE
            }
            val buttonDownloadFiles = Button("Скачать скрипты задачи", style = ButtonStyle.PRIMARY) {
                if (taskType == "text") {
                    display = Display.NONE
                }
            }
            buttonEditScripts.onClick {
                editScripts = !editScripts
                if (editScripts) {
                    buttonEditScripts.text = "Не изменять скрипты задачи"
                    scriptLabel.display = Display.FLEX
                    inputFileLabel.display = Display.INLINEBLOCK
                    addedScriptsFileViewer.display = Display.INLINEBLOCK
                } else {
                    buttonEditScripts.text = "Изменить скрипты задачи"
                    scriptLabel.display = Display.NONE
                    inputFileLabel.display = Display.NONE
                    addedScriptsFileViewer.display = Display.NONE
                }
            }
            buttonDownloadFiles.onClick {
                getScriptFiles()
            }
            val uploadScriptFiles = scriptFilesUpload(this@formPanel, scriptFiles, addedScriptsFileViewer)
            add(Label("Тип задания", className = "separate-form-label"))
            add(
                FormAddTask::format,
                RadioGroup(
                    listOf(
                        "text" to "Консольное",
                        "file" to "SQL"
                    ),
                    taskType
                ) {
                    onChangeLaunch {
                        if (this.value == "file") {
                            buttonEditScripts.display = Display.INLINEBLOCK
                            if (editScripts) {
                                scriptLabel.display = Display.FLEX
                                inputFileLabel.display = Display.INLINEBLOCK
                                addedScriptsFileViewer.display = Display.INLINEBLOCK
                            }
                            if (taskType == "file") {
                                buttonDownloadFiles.display = Display.BLOCK
                            }
                        } else {
                            buttonEditScripts.display = Display.NONE
                            scriptLabel.display = Display.NONE
                            inputFileLabel.display = Display.NONE
                            addedScriptsFileViewer.display = Display.NONE
                            buttonDownloadFiles.display = Display.NONE
                            editScripts = false
                            buttonEditScripts.text = "Изменить скрипты задачи"
                        }
                    }
                },
                required = true,
                requiredMessage = ""
            )
            add(buttonEditScripts)
            add(scriptLabel)
            add(inputFileLabel)
            add(
                FormAddTask::script,
                uploadScriptFiles,
            )
            add(addedScriptsFileViewer)
            add(buttonDownloadFiles)
        }
        val buttonSend = button("Изменить", className = "usually-button")
        buttonSend.onClickLaunch {
            val isValid = validateForm(formPanelEditTask, scriptFiles, editScripts)
            buttonSend.disabled = isValid
            if (isValid) {
                val taskType = formPanelEditTask.getData().format
                val answerFormat = listOf(
                    AnswerFormat(
                        "",
                        taskType
                    )
                )
                val scriptFilesWithContent = if (scriptFiles.isEmpty()) null else scriptFiles
                val formData = FormData().apply {
                    append("name", formPanelEditTask.getData().name ?: "")
                    append("description", formPanelEditTask.getData().description ?: "")
                    append("criterions", formPanelEditTask.getData().criterion ?: "")
                    append("answerFormat", Json.Default.encodeToString(answerFormat))
                    append(
                        "editScripts",
                        if (taskType == "file") editScripts.toString() else true.toString()
                    )
                    if ((scriptFilesWithContent != null) && (taskType == "file") && editScripts) {
                        scriptFilesWithContent.forEach { script ->
                            append(
                                name = "script",
                                value = fileToSend(script)
                            )
                        }
                    }
                }
                sendTask(formData, serverUrl + "task/change/${task.id}", routing)
            }
        }
        document.addEventListener("keydown", { event ->
            if (event is KeyboardEvent && event.keyCode == 13) {
                if (document.activeElement == document.body) {
                    buttonSend.getElement()?.click()
                }
            }
        })
    }

    private fun getScriptFiles() {
        val requestInit = createRequestHeaders(HttpMethod.GET)
        window.fetch(serverUrl + "task/scripts/${task.id}", requestInit).then { response ->
            when (response.status.toInt()) {
                200 -> response.blob().then {
                    val url = URL.createObjectURL(it)
                    val link = document.createElement("a") as HTMLAnchorElement
                    link.href = url
                    link.download = "task_${task.name}.zip"
                    document.body?.appendChild(link)
                    link.click()
                    document.body?.removeChild(link)
                    URL.revokeObjectURL(url)
                }

                400 -> response.json().then {
                    val jsonString = JSON.stringify(it)
                    val responseError =
                        Json.Default.decodeFromString<ResponseError>(jsonString)
                    Toast.danger(responseError.error,
                        ToastOptions(
                            duration = 5000,
                            position = ToastPosition.TOPRIGHT,
                        )
                    )
                }

                404 -> response.json().then {
                    val jsonString = JSON.stringify(it)
                    val responseError =
                        Json.Default.decodeFromString<ResponseError>(jsonString)
                    Toast.danger(responseError.error,
                        ToastOptions(
                            duration = 5000,
                            position = ToastPosition.TOPRIGHT,
                        )
                    )
                }

                else -> Toast.danger("Код ошибки ${response.status}: ${response.statusText}",
                    ToastOptions(
                        duration = 5000,
                        position = ToastPosition.TOPRIGHT,
                    )
                )
            }
        }
    }
}
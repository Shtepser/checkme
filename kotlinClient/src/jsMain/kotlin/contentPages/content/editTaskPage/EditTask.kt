package ru.yarsu.contentPages.content.editTaskPage

import io.kvision.core.Display
import io.kvision.core.onChangeLaunch
import io.kvision.core.onClickLaunch
import io.kvision.core.onInput
import io.kvision.form.FormPanel
import io.kvision.form.check.RadioGroup
import io.kvision.form.formPanel
import io.kvision.form.text.RichText
import io.kvision.form.text.Text
import io.kvision.form.text.TextArea
import io.kvision.form.upload.Upload
import io.kvision.form.upload.getFileWithContent
import io.kvision.html.Button
import io.kvision.html.Div
import io.kvision.html.Label
import io.kvision.html.button
import io.kvision.html.h2
import io.kvision.panel.SimplePanel
import io.kvision.panel.VPanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.types.KFile
import io.kvision.types.base64Encoded
import io.kvision.types.contentType
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.dom.events.InputEvent
import org.w3c.dom.events.KeyboardEvent
import org.w3c.fetch.RequestInit
import org.w3c.files.File
import org.w3c.files.FilePropertyBag
import org.w3c.xhr.FormData
import ru.yarsu.contentPages.content.bundlesPages.BundleViewer
import ru.yarsu.contentPages.content.createRequestHeaders
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.bundle.BundleFormatWithTasks
import ru.yarsu.serializableClasses.task.AnswerFormat
import ru.yarsu.serializableClasses.task.Criterion
import ru.yarsu.serializableClasses.task.FormAddTask
import ru.yarsu.serializableClasses.task.TaskFormat
import ru.yarsu.serializableClasses.task.TaskId
import kotlin.io.encoding.Base64
import kotlin.uuid.Uuid

class EditTask(
    private val task: TaskFormat,
    private val serverUrl: String,
    private val routing: Routing
) : VPanel(className = "TaskAdd") {
    private val scriptFile = mutableListOf<KFile>()
    init {
        h2("Редактирование задачи")
        val formPanelAddTask = formPanel<FormAddTask>(className = "base-form") {
            add(Label("Название", className = "separate-form-label"))
            add(
                FormAddTask::name,
                Text(value = task.name),
                required = true,
                requiredMessage = ""
            )
            add(Label("Описание", className = "separate-form-label"))
            add(
                FormAddTask::description,
                RichText(value = task.description),
                required = true,
                requiredMessage = ""
            )
            add(Label("JSON с критериями задачи", className = "separate-form-label"))
            val criterions = Json.encodeToString(task.criterions)
            val textArea = TextArea(value = criterions)
            add(
                Label("Выберите JSON файл", forId = "input-file-0", className = "file-upload")
            )
            add(
                Upload(accept = listOf(".json")) {
                    this.input.id = "input-file-0"
                    onChangeLaunch {
                        val file = this@Upload.getValue()?.map { file -> this@Upload.getFileWithContent(file) }
                        if (file != null) {
                            val encodedContent = file[0].base64Encoded
                            textArea.value = if (encodedContent != null) {
                                Base64.Default.decode(encodedContent).decodeToString()
                            } else {
                                ""
                            }
                            this@formPanel.getElement()?.dispatchEvent(InputEvent("input"))
                            this@Upload.clearInput()
                        }
                    }
                }
            )
            add(
                FormAddTask::criterion,
                textArea,
                required = true,
                requiredMessage = "",
                validatorMessage = { "Некорректный Json" }
            ) {
                try {
                    val jsonString = it.getValue().toString()
                    val criterions = Json.Default.decodeFromString<Map<String, Criterion>>(jsonString)
                    criterions != null
                } catch (e: Exception) {
                    console.error("Ошибка парсинга JSON: ${e.message}")
                    console.error(e.stackTraceToString())
                    false
                }
            }
            val answerLabel = Label("Вопрос", className = "separate-form-label") {
                display = Display.NONE
            }
            val answerTextArea = TextArea { display = Display.NONE }
            add(Label("Формат ответа", className = "separate-form-label"))
            add(
                FormAddTask::format,
                RadioGroup(
                    listOf(
//                        "text" to "Текст", //если будет нужен функционал с текстовым ответом,
//                        далее в клиенте пока не реализовано
                        "file" to "Файл"
                    )
                ) {
                    onChangeLaunch {
                        if (this.value == "text") {
                            answerLabel.display = Display.BLOCK
                            answerTextArea.display = Display.BLOCK
                        } else {
                            answerLabel.display = Display.NONE
                            answerTextArea.display = Display.NONE
                        }
                    }
                },
                required = true,
                requiredMessage = ""
            )
            add(answerLabel)
            add(
                FormAddTask::answer,
                answerTextArea,
                requiredMessage = "",
            )
            add(Label("Скрипт", className = "separate-form-label"))
            val addedScriptsFileViewer = Div("Файлы не выбраны", className = "files-viewer")
            add(
                Label("Выберите файлы", forId = "input-file-1", className = "file-upload")
            )
            add(
                FormAddTask::script,
                Upload(accept = listOf(".sql"), multiple = true) {
                    this.input.id = "input-file-1"
                    onChangeLaunch {
                        val scriptListFile =
                            this@Upload.getValue()?.map { file -> this@Upload.getFileWithContent(file) } ?: emptyList()
                        scriptFile.addAll(scriptListFile)
                        updateFilesViewer(addedScriptsFileViewer, scriptFile, this@formPanel)
                        this@Upload.clearInput()
                        this@formPanel.getElement()?.dispatchEvent(InputEvent("input"))
                        this@formPanel.validate()
                    }
                },
                validatorMessage = { "" }
            )
            this.validate()
            add(addedScriptsFileViewer)
        }
        val buttonSend = button("Отправить", disabled = true, className = "usually-button")
        formPanelAddTask.onInput {
            buttonSend.disabled = !formPanelAddTask.validate()
        }
        buttonSend.onClickLaunch {
            buttonSend.disabled = true
            val answerFormat = listOf(
                AnswerFormat(
                    formPanelAddTask.getData().answer ?: "",
                    formPanelAddTask.getData().format
                )
            )
            val scriptFilesWithContent = if (scriptFile.isEmpty()) null else scriptFile
            val formData = FormData().apply {
                append("name", formPanelAddTask.getData().name)
                append("description", formPanelAddTask.getData().description)
                append("criterions", formPanelAddTask.getData().criterion)
                append("answerFormat", Json.Default.encodeToString(answerFormat))

                if (scriptFilesWithContent != null) {
                    scriptFilesWithContent.forEach { script ->
                        val scriptEncodedContent = script.base64Encoded
                        val scriptDecodedContent = if (scriptEncodedContent != null) {
                            Base64.Default.decode(scriptEncodedContent).decodeToString()
                        } else {
                            ""
                        }
                        val scriptName = script.name
                        val scriptExpansion = scriptName.split(".").last()
                        val scriptContentType = if (scriptExpansion == "sql") "application/sql" else script.contentType
                        append(
                            name = "script",
                            value = File(
                                arrayOf(scriptDecodedContent),
                                scriptName,
                                FilePropertyBag(type = scriptContentType)
                            )
                        )
                    }
                }
            }
            addTask(formData)
        }
        document.addEventListener("keydown", { event ->
            if (event is KeyboardEvent && event.keyCode == 13) {
                if (document.activeElement == document.body) {
                    buttonSend.getElement()?.click()
                }
            }
        })
    }

    fun updateFilesViewer(filesViewer: Div, files: MutableList<KFile>, form: FormPanel<FormAddTask>) {
        filesViewer.removeAll()
        filesViewer.content = ""
        if (files.isEmpty()) {
            filesViewer.content = "Файлы не выбраны"
        } else {
            files.forEach { kFile ->
                val fileViewer = Div().apply {
                    add(Div(kFile.name))
                    add(Button("Удалить файл", className = "delete-file-button") {
                        onClick {
                            files.remove(kFile)
                            updateFilesViewer(filesViewer, files, form)
                            form.getElement()?.dispatchEvent(InputEvent("input"))
                            form.validate()
                        }
                    })
                }
                filesViewer.add(fileViewer)
            }
        }
    }

    private fun addTask(
        formData: FormData
    ) {
        val requestInit = createRequestHeaders(HttpMethod.POST)
        requestInit.body = formData
        window.fetch(serverUrl + "task/new", requestInit).then { response ->
            when (response.status.toInt()) {
                200 -> response.json().then {
                    val jsonString = JSON.stringify(it)
                    val taskId = Json.Default.decodeFromString<TaskId>(jsonString)
                    routing.navigate("/task/${taskId.taskId}")
                }

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
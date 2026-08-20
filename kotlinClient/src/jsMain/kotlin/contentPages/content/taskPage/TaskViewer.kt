package ru.yarsu.contentPages.content.taskPage

import io.kvision.core.onChangeLaunch
import io.kvision.core.onClick
import io.kvision.core.onClickLaunch
import io.kvision.core.onInput
import io.kvision.form.FormPanel
import io.kvision.form.formPanel
import io.kvision.form.text.TextArea
import io.kvision.form.upload.Upload
import io.kvision.form.upload.getFileWithContent
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.html.Div
import io.kvision.html.Label
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.h2
import io.kvision.html.h3
import io.kvision.panel.HPanel
import io.kvision.panel.VPanel
import io.kvision.panel.hPanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.types.KFile
import io.kvision.types.base64Encoded
import io.kvision.types.contentType
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.dom.events.InputEvent
import org.w3c.files.File
import org.w3c.files.FilePropertyBag
import org.w3c.xhr.FormData
import ru.yarsu.contentPages.Loading
import ru.yarsu.contentPages.content.createRequestHeaders
import ru.yarsu.contentPages.content.hiddenTask.TaskHiddenButton
import ru.yarsu.contentPages.content.getSolutionBlockColorName
import ru.yarsu.contentPages.content.getTaskBlockColorName
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.task.CheckId
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.solution.TaskOrUserSolutionsFormat
import ru.yarsu.serializableClasses.task.SolutionFileList
import ru.yarsu.serializableClasses.task.TaskFormat
import kotlin.io.encoding.Base64

class TaskViewer(
    private val task: TaskFormat,
    private val serverUrl: String,
    private val routing: Routing
) : VPanel(className = "Task") {

    private var solutionFile: KFile? = null

    init {
        h2(task.name)
        if (UserInformationStorage.isAdmin()) {
            button("Решения задачи", style = ButtonStyle.LINK).onClick {
                routing.navigate("solution-list/task/${task.id}")
            }
        }
        val bestResult = if (task.bestScore == -1) {
            "нет результата"
        } else {
            task.bestScore.toString()
        }
        hPanel(className = "sent-solutions") {
            val bestScore = task.bestScore ?: -2
            val highestScore = task.highestScore ?: -2
            div("Ваш лучший результат: $bestResult", className = "best-solution ${getTaskBlockColorName(highestScore, bestScore).cssName}")
            getMySolutions(this)
        }
        h3("Описание")
        div(task.description, className = "task-description", rich = true)
        val taskType = task.answerFormat.first().type
        if (taskType == "file") {
            div("Предоставьте ответ в виде файла с расширением .sql", className = "solution-label")
        } else {
            div("Предоставьте ответ в виде текстового файла", className = "solution-label")
        }
        val solutionTextArea = TextArea {
            readonly = true
        }
        val formPanelSendSolution = formPanel<SolutionFileList>(className = "answer") {
            val addedFileViewer = Div("Файл не выбран", className = "file-viewer")
            add(
                Label("Выберите файл c решением", forId = "input-solution-file", className = "btn btn-secondary")
            )
            add(
                SolutionFileList::file,
                Upload(accept =  if (taskType == "file") listOf(".sql") else null, multiple = false) {
                    this.input.id = "input-solution-file"
                    onChangeLaunch {
                        solutionFile = this@Upload.getValue()?.map { file -> this@Upload.getFileWithContent(file) }[0]
                        val file = solutionFile
                        if (file != null) {
                            val encodedContent = file.base64Encoded
                            solutionTextArea.value = if (encodedContent != null) {
                                Base64.Default.decode(encodedContent).decodeToString()
                            } else {
                                ""
                            }
                        }
                        updateFileViewer(addedFileViewer, solutionFile, this@formPanel, solutionTextArea, taskType)
                        this@Upload.clearInput()
                        this@formPanel.getElement()?.dispatchEvent(InputEvent("input"))
                        this@formPanel.validate()
                    }
                },
                validatorMessage = { "" }
            ) {
                solutionFile != null
            }
            this.validate()
            add(
                addedFileViewer
            )
        }
        val buttonSend = button("Отправить ответ", disabled = true, style = ButtonStyle.PRIMARY)
        formPanelSendSolution.onInput {
            buttonSend.disabled = true
            if (formPanelSendSolution.validate()) {
                buttonSend.disabled = false
            }
        }
        buttonSend.onClickLaunch {
            buttonSend.disabled = true
            val file = solutionFile ?: KFile("", 0)
            val encodedFile = file.base64Encoded
            val decodedFile = if (encodedFile != null) {
                Base64.Default.decode(encodedFile).decodeToString()
            } else {
                ""
            }
            val name = file.name
            val expansion = name.split(".").last()
            val contentType = if (expansion == "sql") "application/sql" else file.contentType
            val formData = FormData().apply {
                append(
                    "ans", value = File(
                        arrayOf(decodedFile),
                        name,
                        FilePropertyBag(type = contentType)
                    )
                )
            }
            this@TaskViewer.removeAll()
            this@TaskViewer.add(Loading("Решение отправлено. Идёт проверка..."))
            createSolution(formData)
        }
        if (UserInformationStorage.isAdmin()) {
            val hiddenButton = TaskHiddenButton(
                serverUrl,
                task.isActual,
                task.id
            )
            this.add(hiddenButton)
            button("Удалить задачу", style = ButtonStyle.DANGER).onClick {
                deleteTask()
            }
        }
    }

    fun updateFileViewer(fileViewer: Div, file: KFile?, form: FormPanel<SolutionFileList>, text: TextArea, taskType: String) {
        fileViewer.removeAll()
        if (file == null) {
            fileViewer.content = "Файл не выбран"
        } else if ((taskType == "file") && (file.name.split(".").last() != "sql")) {
            Toast.danger("Файл недопустимого формата!")
            fileViewer.content = "Файл не выбран"
            solutionFile = null
        } else {
            fileViewer.content = ""
            val file = Div().apply {
                add(text)
                add(Div(file.name))
                add(Button("Удалить файл", style = ButtonStyle.DANGER) {
                    onClick {
                        text.value = ""
                        remove(text)
                        updateFileViewer(fileViewer, null, form, text, taskType)
                        solutionFile = null
                        form.getElement()?.dispatchEvent(InputEvent("input"))
                        form.validate()
                    }
                })
            }
            fileViewer.add(file)
        }
    }

    private fun createSolution(
        formData: FormData
    ) {
        val requestInit = createRequestHeaders(HttpMethod.POST)
        requestInit.body = formData
        window.fetch(serverUrl + "solution/new/${task.id}", requestInit).then { response ->
            when (response.status.toInt()) {
                200 -> response.json().then {
                    val jsonString = JSON.stringify(it)
                    val checkId = Json.Default.decodeFromString<CheckId>(jsonString)
                    routing.navigate("/solution/${checkId.checkId}")
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

    private fun deleteTask() {
        val requestInit = createRequestHeaders(HttpMethod.DELETE)
        window.fetch(serverUrl + "task/delete/${task.id}", requestInit).then { response ->
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
    
    private fun getMySolutions(hPanel: HPanel) {
        hPanel.add(Div("Отправленные решения:", className = "solutions-label"))
        val requestInit = createRequestHeaders(HttpMethod.GET)
        window.fetch(serverUrl + "solution/user-and-task/${task.id}", requestInit).then { response ->
            when (response.status.toInt()) {
                200 -> response.json().then {
                    val jsonString = JSON.stringify(it)
                    val taskSolutions = Json.decodeFromString<TaskOrUserSolutionsFormat>(jsonString)
                    if (taskSolutions.solutions.isEmpty()) {
                        hPanel.add(Div("Решения не найдены", className = "not-found"))
                    } else {
                        taskSolutions.solutions.forEach { solution ->
                            hPanel.add(
                                Div(
                                    solution.totalScore.toString(),
                                    className = "mini-block ${getSolutionBlockColorName(solution.result)}") {
                                    onClick {
                                        routing.navigate("/solution/${solution.id}")
                                    }
                                }
                            )
                        }
                    }
                }

                400 -> response.json().then {
                    val jsonString = JSON.stringify(it)
                    val responseError =
                        Json.Default.decodeFromString<ResponseError>(jsonString)
                    hPanel.add(Div(responseError.error, className = "error-message"))
                }

                else -> hPanel.add(
                    Div(
                        "Код ошибки ${response.status}: ${response.statusText}",
                        className = "error-message"
                    )
                )
            }
        }
    }
}
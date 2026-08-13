package ru.yarsu.contentPages.content.automaticRegistrationPage

import io.kvision.core.onChangeLaunch
import io.kvision.core.onClickLaunch
import io.kvision.core.onInput
import io.kvision.form.FormPanel
import io.kvision.form.formPanel
import io.kvision.form.upload.Upload
import io.kvision.form.upload.getFileWithContent
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.html.Div
import io.kvision.html.Label
import io.kvision.html.button
import io.kvision.panel.HPanel
import io.kvision.panel.vPanel
import io.kvision.rest.HttpMethod
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
import org.w3c.fetch.RequestInit
import org.w3c.files.File
import org.w3c.files.FilePropertyBag
import org.w3c.xhr.FormData
import ru.yarsu.contentPages.content.createRequestHeaders
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.admin.GetSystemPasswords
import ru.yarsu.serializableClasses.signUp.FormLoadStudents
import kotlin.io.encoding.Base64

class AutomaticRegistration(
    serverUrl: String,
) : HPanel(className = "actionsPanel") {
    init {
        vPanel(className = "actionPanel") {
            val studentsDataFile = mutableListOf<KFile>()
            val formAutomaticRegistration = formPanel<FormLoadStudents>(className = "base-form") {
                add(Label("Автоматическая регистрация", className = "separate-form-label"))
                val addedFileViewer = Div("Файл не выбран", className = "files-viewer")
                add(
                    Label("Выберите csv-файл", forId = "data-file", className = "btn btn-secondary")
                )
                add(
                    FormLoadStudents::studentsData,
                    Upload(accept = listOf(".csv")) {
                        this.input.id = "data-file"
                        onChangeLaunch {
                            val dataFile =
                                this@Upload.getValue()?.map { file -> this@Upload.getFileWithContent(file) }
                                    ?: emptyList()
                            studentsDataFile.clear()
                            studentsDataFile.addAll(dataFile)
                            updateFilesViewer(addedFileViewer, studentsDataFile, this@formPanel)
                            this@Upload.clearInput()
                            this@formPanel.getElement()?.dispatchEvent(InputEvent("input"))
                            this@formPanel.validate()
                        }
                    }, validatorMessage = { "" }
                ) {
                    studentsDataFile.isNotEmpty()
                }
                add(
                    addedFileViewer
                )
                this.validate()
                add(
                    addedFileViewer
                )
            }

            val buttonSend = button("Загрузить", disabled = true, style = ButtonStyle.PRIMARY)

            formAutomaticRegistration.onInput {
                buttonSend.disabled = studentsDataFile.isEmpty()
            }

            buttonSend.onClickLaunch {
                buttonSend.disabled = true
                val formData = FormData().apply {
                    val dataFilesWithContent = if (studentsDataFile.isEmpty()) null else studentsDataFile
                    if (dataFilesWithContent != null) {
                        val csvFile = dataFilesWithContent[0]
                        val csvEncodedContent = csvFile.base64Encoded
                        val csvDecodedContent = if (csvEncodedContent != null) {
                            Base64.Default.decode(csvEncodedContent).decodeToString()
                        } else {
                            ""
                        }
                        val contentType = csvFile.contentType
                        append(
                            name = "file",
                            value = File(
                                arrayOf(csvDecodedContent),
                                csvFile.name,
                                FilePropertyBag(type = contentType)
                            ),
                        )
                    }
                }
                registrateStudents(
                    formData = formData,
                    serverUrl = serverUrl,
                    studentsDataFile = studentsDataFile,
                    formAutomaticRegistration = formAutomaticRegistration
                )
            }
        }

        vPanel(className = "actionPanel") {
            val emailsDataFile = mutableListOf<KFile>()
            val formGetSystemPasswords = formPanel<GetSystemPasswords>(className = "base-form") {
                add(Label("Получение системных паролей", className = "separate-form-label"))
                val addedFileViewer = Div("Файл не выбран", className = "files-viewer")
                add(
                    Label("Выберите csv-файл", forId = "emails-file", className = "btn btn-secondary")
                )
                add(
                    GetSystemPasswords::studentsData,
                    Upload(accept = listOf(".csv")) {
                        this.input.id = "emails-file"
                        onChangeLaunch {
                            val dataEmailsFile =
                                this@Upload.getValue()?.map { file -> this@Upload.getFileWithContent(file) }
                                    ?: emptyList()
                            emailsDataFile.clear()
                            emailsDataFile.addAll(dataEmailsFile)
                            updateFilesPasswordsViewer(addedFileViewer, emailsDataFile, this@formPanel)
                            this@Upload.clearInput()
                            this@formPanel.getElement()?.dispatchEvent(InputEvent("input"))
                            this@formPanel.validate()
                        }
                    }, validatorMessage = { "" }
                ) {
                    emailsDataFile.isNotEmpty()
                }
                add(
                    addedFileViewer
                )
                this.validate()
                add(
                    addedFileViewer
                )
            }

            val buttonSendData = button("Скачать", disabled = true, style = ButtonStyle.PRIMARY)

            formGetSystemPasswords.onInput {
                buttonSendData.disabled = emailsDataFile.isEmpty()
            }

            buttonSendData.onClickLaunch {
                buttonSendData.disabled = true
                val formData = FormData().apply {
                    val dataFilesWithContent = if (emailsDataFile.isEmpty()) null else emailsDataFile
                    if (dataFilesWithContent != null) {
                        val csvFile = dataFilesWithContent[0]
                        val csvEncodedContent = csvFile.base64Encoded
                        val csvDecodedContent = if (csvEncodedContent != null) {
                            Base64.Default.decode(csvEncodedContent).decodeToString()
                        } else {
                            ""
                        }
                        val contentType = csvFile.contentType
                        append(
                            name = "file",
                            value = File(
                                arrayOf(csvDecodedContent),
                                csvFile.name,
                                FilePropertyBag(type = contentType)
                            ),
                        )
                    }
                }
                val requestInit = RequestInit()
                requestInit.method = HttpMethod.POST.name
                requestInit.headers = js("{}")
                requestInit.headers["Authentication"] =
                    "Bearer ${UserInformationStorage.getUserInformation()?.token}"
                requestInit.body = formData
                window.fetch(serverUrl + "admin/get-system-passwords", requestInit).then { response ->
                    if (response.status.toInt() == 200) {
                        val fileName = "system-passwords.csv"
                        response.blob().then { blob ->
                            val windowDynamic = window.asDynamic()
                            val documentDynamic = document.asDynamic()
                            val url = windowDynamic.URL.createObjectURL(blob)
                            val a = documentDynamic.createElement("a")
                            a.style.display = "none"
                            a.href = url
                            a.download = fileName
                            documentDynamic.body.appendChild(a)
                            a.click()
                            windowDynamic.URL.revokeObjectURL(url)
                            documentDynamic.body.removeChild(a)
                        }
                        emailsDataFile.clear()
                        formGetSystemPasswords.clearData()
                        buttonSendData.disabled = false
                    } else if (response.status.toInt() == 400) {
                        response.json().then {
                            val jsonString = JSON.stringify(it)
                            val responseError =
                                Json.Default.decodeFromString<ResponseError>(jsonString)
                            Toast.danger(
                                responseError.error,
                                ToastOptions(
                                    duration = 8000,
                                    position = ToastPosition.TOPRIGHT,
                                )
                            )
                        }
                        buttonSendData.disabled = false
                    } else {
                        Toast.danger(
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

    fun updateFilesViewer(filesViewer: Div, files: MutableList<KFile>, form: FormPanel<FormLoadStudents>) {
        filesViewer.removeAll()
        if (files.isEmpty()) {
            filesViewer.content = "Файл не выбран"
        } else {
            files.forEach { kFile ->
                filesViewer.content = ""
                val fileViewer = Div().apply {
                    add(Div(kFile.name))
                    add(Button("Удалить", style = ButtonStyle.DANGER) {
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

    fun updateFilesPasswordsViewer(
        filesViewer: Div,
        files: MutableList<KFile>,
        form: FormPanel<GetSystemPasswords>
    ) {
        filesViewer.removeAll()
        if (files.isEmpty()) {
            filesViewer.content = "Файл не выбран"
        } else {
            files.forEach { kFile ->
                filesViewer.content = ""
                val fileViewer = Div().apply {
                    add(Div(kFile.name))
                    add(Button("Удалить файл", style = ButtonStyle.DANGER) {
                        onClick {
                            files.remove(kFile)
                            updateFilesPasswordsViewer(filesViewer, files, form)
                            form.getElement()?.dispatchEvent(InputEvent("input"))
                            form.validate()
                        }
                    })
                }
                filesViewer.add(fileViewer)
            }
        }
    }

    private fun registrateStudents(
        formData: FormData,
        serverUrl: String,
        studentsDataFile: MutableList<KFile>,
        formAutomaticRegistration: FormPanel<FormLoadStudents>
    ) {
        val requestInit = createRequestHeaders(HttpMethod.POST)
        requestInit.body = formData
        window.fetch(serverUrl + "user/automatic-registration", requestInit).then { response ->
            when (response.status.toInt()) {
                200 -> {
                    studentsDataFile.clear()
                    formAutomaticRegistration.clearData()
                    js("window.location.reload()")
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
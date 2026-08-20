package ru.yarsu.contentPages.content.userPages

import io.kvision.form.FormPanel
import io.kvision.form.formPanel
import io.kvision.form.text.Text
import io.kvision.html.ButtonStyle
import io.kvision.html.Label
import io.kvision.html.button
import io.kvision.html.h2
import io.kvision.modal.Modal
import io.kvision.panel.HPanel
import ru.yarsu.serializableClasses.user.FormChangePassword
import io.kvision.rest.HttpMethod
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import org.w3c.xhr.FormData
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.ResponseError

class ChangeUserPassword(
    private val serverUrl: String,
) : Modal(className = "modal-window ChangeUserPassword") {
    init {
        this.hide()
        h2("Изменение пароля")
        val formPanelChangePassword = formPanel<FormChangePassword>(className = "base-form") {
            add(Label("Пароль", className =  "separate-form-label"))
            add(
                FormChangePassword::oldPassword,
                Text(),
                required = true,
                requiredMessage = "Пожалуйста, введите пароль"
            )
            add(Label("Новый пароль", className =  "separate-form-label"))
            add(
                FormChangePassword::newPassword,
                Text(),
                required = true,
                requiredMessage = "Пожалуйста, введите новый пароль"
            )
        }
        formPanelChangePassword.add(HPanel(className = "change-name-button") {
            button("Изменить", style = ButtonStyle.PRIMARY).onClick {
                val validateForm = formPanelChangePassword.validate()
                if (validateForm) {
                    this@ChangeUserPassword.hide()
                    sendPasswordsResponse(
                        formPanelChangePassword = formPanelChangePassword
                    )
                }
            }
        })
    }

    private fun sendPasswordsResponse(
        formPanelChangePassword: FormPanel<FormChangePassword>,
    ) {
        val requestInit = RequestInit()
        val formData = FormData().apply {
            append("old-password", formPanelChangePassword.getData().oldPassword)
            append("new-password", formPanelChangePassword.getData().newPassword)
        }
        requestInit.method = HttpMethod.POST.name
        requestInit.headers = js("{}")
        requestInit.headers["Authentication"] =
            "Bearer ${UserInformationStorage.getUserInformation()?.token}"
        requestInit.body = formData
        window.fetch(serverUrl + "user/change-password", requestInit).then { response ->
            if (response.status.toInt() == 200) {
                response.json().then {
                    formPanelChangePassword.clearData()
                    Toast.success(
                        "Пароль успешно изменён",
                        ToastOptions(
                            duration = 3000,
                            position = ToastPosition.TOPRIGHT,
                        )
                    )
                }
            } else if (response.status.toInt() == 400) {
                response.json().then {
                    formPanelChangePassword.clearData()
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
            } else {
                formPanelChangePassword.clearData()
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
package ru.yarsu.contentPages.content.addBundlePage

import io.kvision.form.FormPanel
import io.kvision.form.formPanel
import io.kvision.html.Label
import io.kvision.form.text.Text
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.h2
import io.kvision.modal.Modal
import io.kvision.panel.HPanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.xhr.FormData
import ru.yarsu.contentPages.content.createRequestHeaders
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.bundle.BundleId
import ru.yarsu.serializableClasses.bundle.FormAddBundle

class AddBundle(
    private val serverUrl: String,
    private val routing: Routing
) : Modal(className = "modal-window BundleAdd") {
    init {
        this.hide()
        h2("Создание набора")
        val formPanelAddBundle = formPanel<FormAddBundle>(className = "base-form") {
            add(Label("Название набора", className = "separate-form-label"))
            add(
                FormAddBundle::name,
                Text(),
                required = true,
                requiredMessage = "Пожалуйста, введите название набора"
            )
        }
        formPanelAddBundle.add(HPanel(className = "add-bundle-buttons-panel") {
            button("Отправить", style = ButtonStyle.PRIMARY).onClick {
                val validateForm = formPanelAddBundle.validate()
                if (validateForm) {
                    this@AddBundle.hide()
                    createBundle(formPanelAddBundle)
                }
            }
        })
    }

    private fun createBundle(
        formPanelAddBundle: FormPanel<FormAddBundle>
    ) {
        val requestInit = createRequestHeaders(HttpMethod.POST)
        val formData = FormData().apply {
            append("name", formPanelAddBundle.getData().name)
        }
        requestInit.body = formData
        window.fetch(serverUrl + "bundle/new", requestInit).then { response ->
            when (response.status.toInt()) {
                200 -> {
                    response.json().then {
                        val jsonString = JSON.stringify(it)
                        val bundleId = Json.decodeFromString<BundleId>(jsonString)
                        routing.navigate("/bundle/${bundleId.bundleId}")
                    }
                }

                400 -> {
                    response.json().then {
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
                }

                else -> {
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
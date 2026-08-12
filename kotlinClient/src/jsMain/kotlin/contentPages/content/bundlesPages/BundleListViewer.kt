package ru.yarsu.contentPages.content.bundlesPages

import io.kvision.core.onClick
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.panel.VPanel
import io.kvision.panel.hPanel
import io.kvision.routing.Routing
import ru.yarsu.contentPages.content.hiddenBundle.BundleHiddenButton
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.bundle.BundleFormat

class BundleListViewer(
    private val serverUrl: String,
    private val routing: Routing,
    bundleList: List<BundleFormat>
) : VPanel() {
    init {
        for (bundle in bundleList) {
            if (bundle.isActual || UserInformationStorage.isAdmin()) {
                hPanel(className = "bundle-in-list") {
                    val bundleItem = VPanel(className = "bundle-item") {
                        div(bundle.name, className = "name")
                    }.apply {
                        this.onClick {
                            routing.navigate("bundle/${bundle.id}")
                        }
                    }
                    this.add(bundleItem)
                    if (UserInformationStorage.isAdmin()) {
                        button("Изменить", style = ButtonStyle.PRIMARY).onClick {
                            routing.navigate("/bundle/select-bundle-tasks/${bundle.id}")
                        }
                        val hiddenButton = BundleHiddenButton(
                            serverUrl,
                            bundle.isActual,
                            bundle.id,
                            this
                        )
                        this.add(hiddenButton)
                    }
                }
            }
        }
    }
}
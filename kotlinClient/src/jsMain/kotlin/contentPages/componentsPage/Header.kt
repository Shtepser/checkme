package ru.yarsu.contentPages.componentsPage

import io.kvision.dropdown.dropDown
import io.kvision.html.ButtonStyle
import io.kvision.html.TAG
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.tag
import io.kvision.panel.VPanel
import io.kvision.panel.hPanel
import io.kvision.routing.Routing
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import ru.yarsu.contentPages.content.userPages.ChangeUserPassword
import ru.yarsu.localStorage.UserInformationStorage

class Header(
    private val serverUrl: String,
    private val routing: Routing,
    private val routingMainPage: Routing,
) : VPanel() {
    companion object {
        private var autoCloseInstalled = false

        fun installAutoClose() {
            if (autoCloseInstalled) return
            autoCloseInstalled = true

            document.addEventListener("click", { event ->
                val target = event.target as? HTMLElement ?: return@addEventListener
                val current = target.closest(".dropdown") as? HTMLElement
                val dropdowns = document.querySelectorAll(".dropdown")
                for (i in 0 until dropdowns.length) {
                    val element = dropdowns.item(i) as? HTMLElement ?: continue
                    if (element == current) continue
                    (element.querySelector(".dropdown-menu") as? HTMLElement)
                        ?.classList?.remove("show")
                    (element.querySelector(".dropdown-toggle") as? HTMLElement)?.let { element ->
                        element.classList.remove("show")
                        element.setAttribute("aria-expanded", "false")
                    }
                    element.classList.remove("show")
                }
            }, true)
        }
    }

    init {
        installAutoClose()
        hPanel(className = "Header") {
            this.id = "header"
            div("CheckMe", className = "app-title") { id = "id-app-title" }
            div(className = "navigation") {
                if (!UserInformationStorage.isAdmin()) {
                    button(
                        "Наборы заданий",
                        style = ButtonStyle.LINK
                    ) { id = "bundle-list-navigation-student" } .onClick { routingMainPage.navigate("/") }
                }
                if (UserInformationStorage.isAdmin()) {
                    dropDown("Задания", style = ButtonStyle.SECONDARY) {
                        id = "tasks-dropdown"
                        button(
                            "Наборы",
                            style = ButtonStyle.LINK
                        ) { id = "bundle-list-navigation" } .onClick { routingMainPage.navigate("/") }
                        button(
                            "Скрытые наборы",
                            style = ButtonStyle.LINK
                        ) { id = "hidden-bundle-list-navigation" } .onClick { routingMainPage.navigate("/hidden-bundle-list") }
                        button(
                            "Задания",
                            style = ButtonStyle.LINK
                        ) { id = "tasks-navigation" } .onClick { routingMainPage.navigate("/tasks/all") }
                        button(
                            "Скрытые задания",
                            style = ButtonStyle.LINK
                        )  { id = "hidden-task-list-navigation" } .onClick { routingMainPage.navigate("/hidden-task-list") }
                    }
                }
                if (!UserInformationStorage.isAdmin()) {
                    button(
                        "Мои решения",
                        style = ButtonStyle.LINK
                    ) { id = "my-result-list-navigation-student" } .onClick { routingMainPage.navigate("/my-result-list/1") }
                }
                if (UserInformationStorage.isAdmin()) {
                    dropDown("Решения", style = ButtonStyle.SECONDARY) {
                        id = "solutions-dropdown"
                        button(
                            "Мои",
                            style = ButtonStyle.LINK
                        ) { id = "my-result-list-navigation" } .onClick { routingMainPage.navigate("/my-result-list/1") }
                        button(
                            "Все",
                            style = ButtonStyle.LINK
                        ) { id = "solution-list-navigation" } .onClick { routingMainPage.navigate("/solution-list/1") }
                        button(
                            "По задачам",
                            style = ButtonStyle.LINK
                        ) { id = "task-solutions-list-navigation" } .onClick { routingMainPage.navigate("/task-solutions-list/1") }
                    }
                }
                if (UserInformationStorage.isAdmin()) {
                    dropDown("Пользователи", style = ButtonStyle.SECONDARY) {
                        id = "users-dropdown"
                        button(
                            "Список",
                            style = ButtonStyle.LINK
                        ) { id = "user-list-navigation" } .onClick { routingMainPage.navigate("/user-list") }
                        button(
                            "Управление",
                            style = ButtonStyle.LINK
                        ) { id = "user-info-navigation" } .onClick { routingMainPage.navigate("/user-info") }
                        button(
                            "Журнал действий",
                            style = ButtonStyle.LINK
                        ) { id = "journal-navigation" } .onClick { routingMainPage.navigate("/journal/1") }
                    }
                }
            }
            val userName = UserInformationStorage.getUserInformation()
            if (userName == null) {
                routing.navigate("/authorization/sign_in")
            } else {
                div(className = "user-menu") {
                    dropDown(userName.username, style = ButtonStyle.SECONDARY) {
                        id = "user-menu-dropdown"
                        if (!UserInformationStorage.isAdmin()) {
                            val windowChangeUserPassword = ChangeUserPassword(serverUrl)
                            button("Сменить пароль", style = ButtonStyle.PRIMARY) {
                                id = "change-password-navigation"
                                onClick {
                                    windowChangeUserPassword.show()
                                }
                            }
                        }
                        button("Выйти", style = ButtonStyle.PRIMARY) {
                            id = "sign-out-button"
                            onClick {
                                UserInformationStorage.deleteUserInformation()
                                routing.navigate("/authorization/sign_in")
                            }
                        }
                    }
                }
            }
        }
        tag(TAG.HR)
    }
}
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
import ru.yarsu.localStorage.UserInformationStorage

class Header(
    private val routing: Routing,
    private val routingMainPage: Routing,
) : VPanel() {
    companion object {
        private var autoCloseInstalled = false

        fun installAutoClose() {
            if (autoCloseInstalled) return
            autoCloseInstalled = true

            document.addEventListener("click", { ev ->
                val target = ev.target as? HTMLElement ?: return@addEventListener
                val current = target.closest(".dropdown") as? HTMLElement
                val dropdowns = document.querySelectorAll(".dropdown")
                for (i in 0 until dropdowns.length) {
                    val node = dropdowns.item(i) as? HTMLElement ?: continue
                    if (node == current) continue
                    (node.querySelector(".dropdown-menu") as? HTMLElement)
                        ?.classList?.remove("show")
                    (node.querySelector(".dropdown-toggle") as? HTMLElement)?.let { toggle ->
                        toggle.classList.remove("show")
                        toggle.setAttribute("aria-expanded", "false")
                    }
                    node.classList.remove("show")
                }
            }, true)
        }
    }

    init {
        installAutoClose()
        hPanel(className = "Header") {
            div("CheckMe", className = "app-title")
            if (!UserInformationStorage.isAdmin()) {
                button(
                    "Наборы заданий",
                    style = ButtonStyle.LINK
                ).onClick { routingMainPage.navigate("/") }
            }
            if (UserInformationStorage.isAdmin()) {
                dropDown("Задания", style = ButtonStyle.SECONDARY) {
                    button(
                        "Наборы",
                        style = ButtonStyle.LINK
                    ).onClick { routingMainPage.navigate("/") }
                    button(
                        "Скрытые наборы",
                        style = ButtonStyle.LINK
                    ).onClick { routingMainPage.navigate("/hidden-bundle-list") }
                    button(
                        "Задания",
                        style = ButtonStyle.LINK
                    ).onClick { routingMainPage.navigate("/tasks/all") }
                    button(
                        "Скрытые задания",
                        style = ButtonStyle.LINK
                    ).onClick { routingMainPage.navigate("/hidden-task-list") }
                }
            }
            if (!UserInformationStorage.isAdmin()) {
                button(
                    "Мои решения",
                    style = ButtonStyle.LINK
                ).onClick { routingMainPage.navigate("/my-result-list/1") }
            }
            if (UserInformationStorage.isAdmin()) {
                dropDown("Решения", style = ButtonStyle.SECONDARY) {
                    button(
                        "Мои",
                        style = ButtonStyle.LINK
                    ).onClick { routingMainPage.navigate("/my-result-list/1") }
                    button(
                        "Все",
                        style = ButtonStyle.LINK
                    ).onClick { routingMainPage.navigate("/solution-list/1") }
                    button(
                        "По задачам",
                        style = ButtonStyle.LINK
                    ).onClick { routingMainPage.navigate("/task-solutions-list/1") }
                }
            }
            if (UserInformationStorage.isAdmin()) {
                dropDown("Пользователи", style = ButtonStyle.SECONDARY) {
                    button(
                        "Список",
                        style = ButtonStyle.LINK
                    ).onClick { routingMainPage.navigate("/user-list") }
                    button(
                        "Управление",
                        style = ButtonStyle.LINK
                    ).onClick { routingMainPage.navigate("/user-info") }
                    button(
                        "Журнал действий",
                        style = ButtonStyle.LINK
                    ).onClick { routingMainPage.navigate("/journal/1") }
                }
            }
            val userName = UserInformationStorage.getUserInformation()
            if (userName == null) {
                routing.navigate("/authorization/sign_in")
            } else {
                dropDown(userName.username, style = ButtonStyle.SECONDARY) {
                    if (!UserInformationStorage.isAdmin()) {
                        button("Сменить пароль", style = ButtonStyle.PRIMARY) {
                            onClick {
                                routingMainPage.navigate("/user/change-password")
                            }
                        }
                    }
                    button("Выйти", style = ButtonStyle.PRIMARY) {
                        onClick {
                            UserInformationStorage.deleteUserInformation()
                            routing.navigate("/authorization/sign_in")
                        }
                    }
                }
            }
        }
        tag(TAG.HR)
    }
}
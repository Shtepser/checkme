package ru.yarsu.contentPages.componentsPage

import io.kvision.html.TAG
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.tag
import io.kvision.panel.VPanel
import io.kvision.panel.hPanel
import io.kvision.routing.Routing
import ru.yarsu.localStorage.UserInformationStorage

class Header(
    private val routing: Routing,
    private val routingMainPage: Routing,
) : VPanel() {
    init {
        hPanel(className = "Header") {
            div("CheckMe", className = "app-title")
            div(className = "navigation") {
                button(
                    "Наборы задач",
                    className = "navigation-button"
                ).onClick { routingMainPage.navigate("/") }
                if (UserInformationStorage.isAdmin()) {
                    button(
                        "Список скрытых наборов",
                        className = "navigation-button"
                    ).onClick { routingMainPage.navigate("/hidden-bundle-list") }
                }
                if (UserInformationStorage.isAdmin()) {
                    button(
                        "Список задач",
                        className = "navigation-button"
                    ).onClick { routingMainPage.navigate("/tasks/all") }
                    button(
                        "Список скрытых задач",
                        className = "navigation-button"
                    ).onClick { routingMainPage.navigate("/hidden-task-list") }
                }
                button(
                    "Мои решения",
                    className = "navigation-button"
                ).onClick { routingMainPage.navigate("/my-result-list/1") }
                if (UserInformationStorage.isAdmin()) {
                    button(
                        "Все решения",
                        className = "navigation-button"
                    ).onClick { routingMainPage.navigate("/solution-list/1") }
                    button(
                        "Решения по задачам",
                        className = "navigation-button"
                    ).onClick { routingMainPage.navigate("/task-solutions-list/1") }
                    button(
                        "Пользователи",
                        className = "navigation-button"
                    ).onClick { routingMainPage.navigate("/user-list") }
                    button(
                        "Информация о пользователях",
                        className = "navigation-button"
                    ).onClick { routingMainPage.navigate("/user-info") }
                    button(
                        "Журнал",
                        className = "navigation-button"
                    ).onClick { routingMainPage.navigate("/journal/1") }
                }
            }
            val userName = UserInformationStorage.getUserInformation()
            if (userName == null) {
                routing.navigate("/authorization/sign_in")
            } else {
                div(userName.username, className = "username")
            }
            if (!UserInformationStorage.isAdmin()) {
                button("Сменить пароль", className = "usually-button") {
                    onClick {
                        routingMainPage.navigate("/user/change-password")
                    }
                }
            }
            button("Выйти", className = "usually-button signout") {
                onClick {
                    UserInformationStorage.deleteUserInformation()
                    routing.navigate("/authorization/sign_in")
                }
            }
        }
        tag(TAG.HR)
    }
}
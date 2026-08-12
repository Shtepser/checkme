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
import ru.yarsu.localStorage.UserInformationStorage

class Header(
    private val routing: Routing,
    private val routingMainPage: Routing,
) : VPanel() {
    init {
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



//            div(className = "navigation") {
//                button(
//                    "Наборы задач",
//                    className = "navigation-button"
//                ).onClick { routingMainPage.navigate("/") }
//                if (UserInformationStorage.isAdmin()) {
//                    button(
//                        "Список скрытых наборов",
//                        className = "navigation-button"
//                    ).onClick { routingMainPage.navigate("/hidden-bundle-list") }
//                }
//                if (UserInformationStorage.isAdmin()) {
//                    button(
//                        "Список задач",
//                        className = "navigation-button"
//                    ).onClick { routingMainPage.navigate("/tasks/all") }
//                    button(
//                        "Список скрытых задач",
//                        className = "navigation-button"
//                    ).onClick { routingMainPage.navigate("/hidden-task-list") }
//                }
//                button(
//                    "Мои решения",
//                    className = "navigation-button"
//                ).onClick { routingMainPage.navigate("/my-result-list/1") }
//                if (UserInformationStorage.isAdmin()) {
//                    button(
//                        "Все решения",
//                        className = "navigation-button"
//                    ).onClick { routingMainPage.navigate("/solution-list/1") }
//                    button(
//                        "Решения по задачам",
//                        className = "navigation-button"
//                    ).onClick { routingMainPage.navigate("/task-solutions-list/1") }
//                    button(
//                        "Пользователи",
//                        className = "navigation-button"
//                    ).onClick { routingMainPage.navigate("/user-list") }
//                    button(
//                        "Информация о пользователях",
//                        className = "navigation-button"
//                    ).onClick { routingMainPage.navigate("/user-info") }
//                    button(
//                        "Журнал",
//                        className = "navigation-button"
//                    ).onClick { routingMainPage.navigate("/journal/1") }
//                }
//            }
            val userName = UserInformationStorage.getUserInformation()
            if (userName == null) {
                routing.navigate("/authorization/sign_in")
            } else {
//                div(userName.username, className = "username")
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
//            if (!UserInformationStorage.isAdmin()) {
//                button("Сменить пароль", className = "usually-button") {
//                    onClick {
//                        routingMainPage.navigate("/user/change-password")
//                    }
//                }
//            }
//            button("Выйти", className = "usually-button signout") {
//                onClick {
//                    UserInformationStorage.deleteUserInformation()
//                    routing.navigate("/authorization/sign_in")
//                }
//            }
        }
        tag(TAG.HR)
    }
}
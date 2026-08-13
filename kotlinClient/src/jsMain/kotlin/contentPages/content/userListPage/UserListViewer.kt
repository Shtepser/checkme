package ru.yarsu.contentPages.content.userListPage

import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.panel.VPanel
import io.kvision.routing.Routing
import ru.yarsu.serializableClasses.user.UserInList

class UserListViewer(
    userList: List<UserInList>,
    private val routing: Routing
) : VPanel(className = "UserList") {
    init {
        for (user in userList) {
            div(className = "user-block") {
                div("${user.surname} ${user.name} (${user.login})")
                button("Решения пользователя", style = ButtonStyle.LINK).onClick {
                    routing.navigate("solution-list/user/${user.id}")
                }
            }
        }
    }
}
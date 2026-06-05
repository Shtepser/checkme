package ru.yarsu.contentPages.content.solutionsPages

import io.kvision.core.onClick
import io.kvision.html.div
import io.kvision.panel.VPanel
import io.kvision.panel.vPanel
import io.kvision.routing.Routing
import kotlinx.datetime.LocalDateTime
import ru.yarsu.serializableClasses.solution.SolutionInAdminListsFormat

class AllSolutionsViewer(
    solutionListForAdmin: List<SolutionInAdminListsFormat>,
    private val routing: Routing
) : VPanel() {
    init {
        for (solution in solutionListForAdmin){
            vPanel(className = "block") {
                if (solution.user != null){
                    div("Пользователь: ${solution.user.surname} ${solution.user.name}")
                }
                if (solution.task != null) {
                    div("Задача: ${solution.task.name}")
                }
                div("Статус: ${solution.status}")
                if ((solution.status == "Проверено")) {
                    val score = solution.totalScore
                    div("Результат: $score")
                }
                val dateTime = LocalDateTime.parse(solution.date).let { LocalDateTime(it.year, it.month, it.day, it.hour, it.minute, it.second) }
                div("${solution.status} - ${dateTime.date} ${dateTime.time}")
            }.onClick {
                routing.navigate("solution/${solution.id}")
            }
        }
    }
}
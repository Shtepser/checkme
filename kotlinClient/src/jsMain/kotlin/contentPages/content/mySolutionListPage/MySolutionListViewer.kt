package ru.yarsu.contentPages.content.mySolutionListPage

import io.kvision.core.onClick
import io.kvision.html.div
import io.kvision.panel.VPanel
import io.kvision.panel.vPanel
import io.kvision.routing.Routing
import kotlinx.datetime.LocalDateTime
import ru.yarsu.serializableClasses.solution.SolutionInAdminListsFormat

class MySolutionListViewer(
    solutionList: List<SolutionInAdminListsFormat>,
    private val routing: Routing
) : VPanel() {
    init {
        for (solution in solutionList) {
            vPanel(className = "block") {
                div("Статус: ${solution.status}")
                if ((solution.status == "Проверено")) {
                    val score = solution.totalScore
                    div("Результат: $score")
                }
                val dateTime = LocalDateTime.parse(solution.date).let { LocalDateTime(it.year, it.month, it.day, it.hour, it.minute, it.second) }
                div("${solution.status} - ${dateTime.date} ${dateTime.time}")
            }.onClick {
                routing.navigate("/solution/${solution.id}")
            }
        }
    }
}
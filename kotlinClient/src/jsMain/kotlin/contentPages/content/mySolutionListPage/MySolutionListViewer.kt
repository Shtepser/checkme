package ru.yarsu.contentPages.content.mySolutionListPage

import io.kvision.core.onClick
import io.kvision.html.div
import io.kvision.panel.VPanel
import io.kvision.panel.vPanel
import io.kvision.routing.Routing
import kotlinx.datetime.LocalDateTime
import ru.yarsu.contentPages.content.getSolutionBlockColorName
import ru.yarsu.serializableClasses.solution.SolutionInAdminListsFormat

class MySolutionListViewer(
    solutionList: List<SolutionInAdminListsFormat>,
    private val routing: Routing
) : VPanel() {
    init {
        solutionList.forEachIndexed { index, solution ->
            vPanel(className = "solution-block ${getSolutionBlockColorName(solution.result)}") {
                id = "solution-block-$index"
                div(solution.status)
                if ((solution.status == "Проверено")) {
                    val score = solution.totalScore
                    div("$score")
                }
                val dateTime = LocalDateTime.parse(solution.date).let { LocalDateTime(it.year, it.month, it.day, it.hour, it.minute, it.second) }
                div("${dateTime.date} ${dateTime.time}")
            }.onClick {
                routing.navigate("/solution/${solution.id}")
            }
        }
    }
}
package ru.yarsu.contentPages.content.solutionsPages

import io.kvision.core.onClick
import io.kvision.html.TAG
import io.kvision.html.div
import io.kvision.html.h4
import io.kvision.html.tag
import io.kvision.panel.VPanel
import io.kvision.panel.vPanel
import io.kvision.routing.Routing
import kotlinx.datetime.LocalDateTime
import ru.yarsu.contentPages.content.getSolutionBlockColorName
import ru.yarsu.serializableClasses.solution.SolutionsGroupByTask

class AllSolutionsGroupByTaskViewer(
    solutionsGroupByTask: List<SolutionsGroupByTask>,
    private val routing: Routing
) : VPanel() {
    init {
        for (taskSolutions in solutionsGroupByTask) {
            vPanel(className = "block") {
                h4("Задача ${taskSolutions.task.name}") {
                    onClick {
                        routing.navigate("task/${taskSolutions.task.id}")
                    }
                }
                tag(TAG.HR)
                if (!taskSolutions.solutions.isEmpty()) {
                    for (solution in taskSolutions.solutions) {
                        vPanel(className = "solution-block-${getSolutionBlockColorName(solution.result)}") {
                            div("${solution.user.surname} ${solution.user.name}")
                            div(solution.status)
                            if ((solution.status == "Проверено")) {
                                val score = solution.totalScore
                                div("$score")
                            }
                            val dateTime = LocalDateTime.parse(solution.date).let { LocalDateTime(it.year, it.month, it.day, it.hour, it.minute, it.second) }
                            div("${dateTime.date} ${dateTime.time}")
                            onClick {
                                routing.navigate("solution/${solution.id}")
                            }
                        }
                        tag(TAG.HR)
                    }
                } else {
                    div("Решения не найдены", className = "not-found")
                }
            }
        }
    }
}
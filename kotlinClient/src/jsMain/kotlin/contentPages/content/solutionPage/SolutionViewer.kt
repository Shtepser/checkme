package ru.yarsu.contentPages.content.solutionPage

import io.kvision.core.onClick
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.h2
import io.kvision.panel.VPanel
import io.kvision.panel.hPanel
import io.kvision.routing.Routing
import ru.yarsu.contentPages.Loading
import ru.yarsu.serializableClasses.solution.SolutionFormat

class SolutionViewer(
    private val solution: SolutionFormat,
    private val routing: Routing
) : VPanel() {
    init {
        if ((solution.status == "В процессе") || (solution.result == null)) {
            add(Loading("Проверяем эту задачу"))
        } else if (solution.status == "Проверено") {
            val score = solution.totalScore
            h2("Результат: $score")
            button("Перейти к заданию", style = ButtonStyle.LINK).onClick {
                routing.navigate("task/${solution.task.id}")
            }
            solution.result.values.forEach { (score, message) ->
                hPanel(className="criteria-list") {
                    div(message, className=if (score > 0) "criteria-message criteria-passed" else "criteria-message criteria-failed")
                    div(score.toString(), className="criteria-score")
                }
            }
        }
    }
}
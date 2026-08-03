package ru.yarsu.contentPages.content.mySolutionListPage

import io.kvision.core.onClick
import io.kvision.html.div
import io.kvision.html.h3
import io.kvision.panel.VPanel
import io.kvision.panel.vPanel
import io.kvision.routing.Routing
import ru.yarsu.contentPages.content.getTaskBlockColorName
import ru.yarsu.serializableClasses.solution.SolutionInMyListFormat

class MyResultListViewer(
    resultList: List<SolutionInMyListFormat>,
    private val routing: Routing
) : VPanel() {
    init {
        resultList.forEachIndexed { index, bundle ->
            vPanel(className = "bundle-block") {
                h3(bundle.bundleName, className = "bundle-name") { id = "bundle-name-h3-${index + 1}" }
                bundle.taskWithBestResult.forEachIndexed { index, task ->
                    val taskBlockName = getTaskBlockColorName(task.highestScore, task.bestSolution)
                    vPanel(className = "task-block ${taskBlockName.cssName}") {
                        id = "task-block-${index + 1}"
                        div(task.taskName)
                        val bestSolution = task.bestSolution
                        if (bestSolution != -1) {
                            div("$bestSolution/${task.highestScore}") {}
                        }
                        div(taskBlockName.message)
                    }.onClick {
                        routing.navigate("/my-solution-list/${task.taskId}")
                    }
                }
            }
        }
    }
}
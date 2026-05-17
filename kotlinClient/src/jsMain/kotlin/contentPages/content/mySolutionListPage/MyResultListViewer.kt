package ru.yarsu.contentPages.content.mySolutionListPage

import io.kvision.core.onClick
import io.kvision.html.div
import io.kvision.html.h3
import io.kvision.panel.VPanel
import io.kvision.panel.vPanel
import io.kvision.routing.Routing
import ru.yarsu.serializableClasses.solution.SolutionInMyListFormat


class MyResultListViewer(
    resultList: List<SolutionInMyListFormat>,
    private val routing: Routing
) : VPanel() {
    init {
        for (bundle in resultList) {
            vPanel(className = "bundle-block") {
                h3(bundle.bundleName, className = "bundle-name")
                for (task in bundle.taskWithBestResult){
                    val taskBlockName = getTaskBlockName(task.highestScore, task.bestSolution)
                    vPanel(className = "task-block-${taskBlockName.cssName}") {
                        div("Задача: ${task.taskName}")
                        div("Максимальное количество баллов: ${task.highestScore}")
                        val bestSolution = task.bestSolution
                        if (bestSolution != -1) {
                            div("Ваше лучшее решение: $bestSolution") {}
                        }
                        div(taskBlockName.message)
                    }.onClick {
                        routing.navigate("/my-solution-list/${task.taskId}")
                    }
                }
            }
        }
    }

    private fun getTaskBlockName(score: Int, result: Int): Result {
        return if (result < 0) {
            Result.NO
        } else if (result == 0) {
            Result.INCORRECT
        } else if (result < score) {
            Result.PARTIAL
        } else {
            Result.CORRECT
        }
    }
}

enum class Result(val message: String, val cssName: String) {
    NO("Нет решений.", "no"),
    INCORRECT("Задача не решена. Нажмите, чтобы посмотреть все отправленные решения.", "incorrect"),
    PARTIAL("Задача решена частично. Нажмите, чтобы посмотреть все отправленные решения.", "partial"),
    CORRECT("Задач решена. Нажмите, чтобы посмотреть все отправленные решения.", "correct"),
}
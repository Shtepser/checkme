package ru.yarsu.contentPages.content.solutionsPages

import io.kvision.core.onClick
import io.kvision.html.Button
import io.kvision.html.Div
import io.kvision.panel.SimplePanel
import io.kvision.panel.VPanel
import io.kvision.routing.Routing
import io.kvision.tabulator.ColumnDefinition
import io.kvision.tabulator.Editor
import io.kvision.tabulator.TableType
import io.kvision.tabulator.TabulatorOptions
import io.kvision.tabulator.tabulator
import kotlinx.serialization.json.Json
import ru.yarsu.serializableClasses.solution.IdScore
import ru.yarsu.serializableClasses.solution.SolutionInformation
import ru.yarsu.serializableClasses.solution.SolutionsTable

class AllSolutionsTableViewer(
    private val routing: Routing,
    private val solutionsTable: SolutionsTable,
    private val downloadButton: Button,
): SimplePanel() {
    private fun getListMaxScore(solutions: List<SolutionInformation>) : List<IdScore> {
        return solutionsTable.tasks.map { task ->
            val max = solutions.filter { it.taskId == task.id }.maxOfOrNull { solution ->
                solution.result?.values?.map { it.score }?.toList()?.sum() ?: 0
            } ?: 0
            IdScore(task.id, max)
        }
    }

    private fun getData() : List<Map<String, String>> {
        return solutionsTable.solutions.toList().mapIndexed { index, user ->
            val userStats = solutionsTable.users.find { it.id == user.first }
            val login = userStats?.login ?: "None"
            val surname = userStats?.surname ?: "None"
            val name = userStats?.name ?: "None"
            val row = mutableMapOf<String, String>()
            row["id"] = user.first.toString()
            row["solutions"] = Json.Default.encodeToString(user.second)
            val listMaxScore = getListMaxScore(user.second)
            for (max in listMaxScore) {
                row["taskId${max.id}"] = max.score.toString()
            }
            row["login"] = login
            row["surname"] = surname
            row["name"] = name
            row
        }
    }

    private val columns = listOf<ColumnDefinition<Map<String, String>>>(
        ColumnDefinition(
            "Логин",
            field = "login",
            headerFilter = Editor.INPUT
        ),
        ColumnDefinition(
            "Фамилия",
            field = "surname",
            headerFilter = Editor.INPUT
        ),
        ColumnDefinition(
            "Имя",
            field = "name",
            headerFilter = Editor.INPUT
        ),
    ) + solutionsTable.tasks.mapIndexed { index, title ->
        val taskId = title.id
        ColumnDefinition(
            title.name,
            field = "taskId$taskId",
            headerSort = false,
            formatterComponentFunction = { _, _, data ->
                val solutions = Json.Default.decodeFromString<List<SolutionInformation>>(
                    data.getValue("solutions")
                ).filter { it.taskId == taskId }
                VPanel().apply {
                    this.addAll(
                        solutions.filter {
                            solution -> solution.totalScore == solutions.maxOf {solution -> solution.totalScore ?: -1}
                        }.map { solution ->
                            val score = solution.totalScore
                            Div("$score", className = "btn btn-link").apply {
                                this.onClick {
                                    routing.navigate("/solution/${solution.id}")
                                }
                            }
                        }
                    )
                }
            }
        )
    }

    init {
        val table = tabulator(data = getData(), false,
            options = TabulatorOptions(
                columns = columns
            ),
            types = setOf(TableType.BORDERED)
        )
        downloadButton.onClick {
            table.downloadCSV("solutionsTable")
        }
    }
}
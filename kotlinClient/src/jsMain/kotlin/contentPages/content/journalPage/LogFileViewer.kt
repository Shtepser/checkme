package ru.yarsu.contentPages.content.journalPage

import io.kvision.html.div
import io.kvision.panel.VPanel
import io.kvision.panel.hPanel
import io.kvision.panel.vPanel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import ru.yarsu.serializableClasses.logger.LogFormat

class LogFileViewer(
    logs: List<LogFormat>
) : VPanel() {
    init {
        this.id = "logsContainer"
        for (log in logs) {
            vPanel(className = "${log.level.lowercase()}-block") {
                hPanel(className = "space-between") {
                    div("${log.userSurname} ${log.userName} (id = ${log.userId}) совершил действие:")
                    div(log.date.dateTimeFormat(), className = "date-item")
                }
                div("${log.action} - ${log.message}", className = "action-item")
            }
        }
    }

    private fun String.dateTimeFormat(): String {
        val parserFormat = LocalDateTime.Format {
            year()
            char('-')
            monthNumber()
            char('-')
            day()
            char(' ')
            hour()
            char(':')
            minute()
            char(':')
            second()
            char(',')
            secondFraction(fixedLength = 3)
        }

        val parsedDateTime = LocalDateTime.parse(this, parserFormat)

        val outputFormat = LocalDateTime.Format {
            day()
            char('.')
            monthNumber()
            char('.')
            year()
            char(' ')
            hour()
            char(':')
            minute()
            char(':')
            second()
            char(',')
            secondFraction(fixedLength = 3)
        }

        return parsedDateTime.format(outputFormat)
    }
}
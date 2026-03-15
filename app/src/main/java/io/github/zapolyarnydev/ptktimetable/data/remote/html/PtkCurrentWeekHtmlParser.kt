package io.github.zapolyarnydev.ptktimetable.data.remote.html

import io.github.zapolyarnydev.ptktimetable.data.model.PtkCurrentWeekType
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class PtkCurrentWeekHtmlParser {

    fun parseCurrentWeekType(
        html: String,
        today: LocalDate = LocalDate.now()
    ): PtkCurrentWeekType {
        return parseWeekTypeForDate(html, today)
    }

    fun parseWeekTypeForDate(
        html: String,
        date: LocalDate
    ): PtkCurrentWeekType {
        val document = Jsoup.parse(html)
        val calendarRoot = findCalendarRoot(document.body())

        parseByRanges(calendarRoot, date)?.let { return it }
        parseByHighlightedCell(calendarRoot)?.let { return it }
        parseByInlineText(document.text())?.let { return it }

        return PtkCurrentWeekType.UNKNOWN
    }

    private fun parseByRanges(
        calendarRoot: Element?,
        today: LocalDate
    ): PtkCurrentWeekType? {
        val rows = calendarRoot
            ?.select("table.viewtable tr")
            .orEmpty()

        for (row in rows) {
            val cells = row.select("td")
            if (cells.size < 4) continue

            val upper = parseRangeCell(cells[1])
            if (upper != null && !today.isBefore(upper.first) && !today.isAfter(upper.second)) {
                return PtkCurrentWeekType.UPPER
            }

            val lower = parseRangeCell(cells[3])
            if (lower != null && !today.isBefore(lower.first) && !today.isAfter(lower.second)) {
                return PtkCurrentWeekType.LOWER
            }
        }

        return null
    }

    private fun parseByHighlightedCell(calendarRoot: Element?): PtkCurrentWeekType? {
        val rows = calendarRoot
            ?.select("table.viewtable tr")
            .orEmpty()

        for (row in rows) {
            val cells = row.select("td")
            if (cells.size < 4) continue

            val upperStyle = cells[1].attr("style").lowercase(Locale.ROOT)
            if (upperStyle.contains("silver")) return PtkCurrentWeekType.UPPER

            val lowerStyle = cells[3].attr("style").lowercase(Locale.ROOT)
            if (lowerStyle.contains("silver")) return PtkCurrentWeekType.LOWER
        }

        return null
    }

    private fun parseByInlineText(textRaw: String): PtkCurrentWeekType? {
        val text = textRaw.lowercase(Locale.ROOT)
        return when {
            CURRENT_WEEK_LOWER.containsMatchIn(text) -> PtkCurrentWeekType.LOWER
            CURRENT_WEEK_UPPER.containsMatchIn(text) -> PtkCurrentWeekType.UPPER
            else -> null
        }
    }

    private fun findCalendarRoot(root: Element?): Element? {
        val header = root
            ?.select("h3")
            ?.firstOrNull { it.text().contains("Календарь", ignoreCase = true) }
            ?: return null

        return header.closest(".block_3padding") ?: header.parent()
    }

    private fun parseRangeCell(cell: Element): Pair<LocalDate, LocalDate>? {
        val match = DATE_RANGE_REGEX.find(cell.text()) ?: return null
        val start = parseDate(match.groupValues[1]) ?: return null
        val end = parseDate(match.groupValues[2]) ?: return null
        return start to end
    }

    private fun parseDate(value: String): LocalDate? {
        return runCatching { LocalDate.parse(value.trim(), DATE_FORMATTER) }.getOrNull()
    }

    private companion object {
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val DATE_RANGE_REGEX = Regex("(\\d{2}\\.\\d{2}\\.\\d{4})\\s*-\\s*(\\d{2}\\.\\d{2}\\.\\d{4})")
        val CURRENT_WEEK_LOWER = Regex("\\(\\s*нижн(?:яя|\\.)?\\s*\\)")
        val CURRENT_WEEK_UPPER = Regex("\\(\\s*верхн(?:яя|\\.)?\\s*\\)")
    }
}

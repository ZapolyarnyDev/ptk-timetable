package io.github.zapolyarnydev.ptktimetable.data.remote.html

import io.github.zapolyarnydev.ptktimetable.data.model.PtkCurrentWeekType
import org.jsoup.Jsoup
import java.util.Locale

class PtkCurrentWeekHtmlParser {

    fun parseCurrentWeekType(html: String): PtkCurrentWeekType {
        val text = Jsoup.parse(html).text().lowercase(Locale.ROOT).replace('ё', 'е')
        return when {
            CURRENT_WEEK_LOWER.containsMatchIn(text) -> PtkCurrentWeekType.LOWER
            CURRENT_WEEK_UPPER.containsMatchIn(text) -> PtkCurrentWeekType.UPPER
            else -> PtkCurrentWeekType.UNKNOWN
        }
    }

    private companion object {
        val CURRENT_WEEK_LOWER = Regex("\\(\\s*нижн(?:яя|\\.?)\\s*\\)")
        val CURRENT_WEEK_UPPER = Regex("\\(\\s*верхн(?:яя|\\.?)\\s*\\)")
    }
}

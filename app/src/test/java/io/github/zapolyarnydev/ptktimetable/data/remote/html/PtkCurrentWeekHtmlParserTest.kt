package io.github.zapolyarnydev.ptktimetable.data.remote.html

import io.github.zapolyarnydev.ptktimetable.data.model.PtkCurrentWeekType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class PtkCurrentWeekHtmlParserTest {

    private val parser = PtkCurrentWeekHtmlParser()

    @Test
    fun `parseCurrentWeekType resolves week by calendar date ranges`() {
        val html = """
            <html><body>
            <div class="block_3padding">
              <h3>Календарь</h3>
              Сегодня 15.03.2026, <b>()</b> неделя.
              <table class="viewtable">
                <tr><th></th><th>Верхние недели:</th><th></th><th>Нижние недели:</th></tr>
                <tr><td>5</td><td>02.03.2026 - 07.03.2026</td><td>6</td><td>09.03.2026 - 14.03.2026</td></tr>
                <tr><td>7</td><td>16.03.2026 - 21.03.2026</td><td>8</td><td>23.03.2026 - 28.03.2026</td></tr>
              </table>
            </div>
            </body></html>
        """.trimIndent()

        val lower = parser.parseCurrentWeekType(
            html = html,
            today = LocalDate.of(2026, 3, 10)
        )
        val upper = parser.parseCurrentWeekType(
            html = html,
            today = LocalDate.of(2026, 3, 17)
        )

        assertEquals(PtkCurrentWeekType.LOWER, lower)
        assertEquals(PtkCurrentWeekType.UPPER, upper)
    }
}


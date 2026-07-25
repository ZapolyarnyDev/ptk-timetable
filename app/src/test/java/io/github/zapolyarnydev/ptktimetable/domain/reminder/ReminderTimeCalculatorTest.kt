package io.github.zapolyarnydev.ptktimetable.domain.reminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class ReminderTimeCalculatorTest {

    private val calculator = ReminderTimeCalculator(ZoneId.of("Europe/Moscow"))

    @Test
    fun `calculate subtracts reminder minutes in the lesson time zone`() {
        val trigger = calculator.calculate(
            lessonDate = LocalDate.of(2026, 7, 25),
            lessonStartTime = LocalTime.of(10, 20),
            minutesBefore = 15,
        )

        assertEquals(Instant.parse("2026-07-25T07:05:00Z"), trigger)
    }

    @Test
    fun `calculate rejects zero and negative offsets`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.calculate(LocalDate.of(2026, 7, 25), LocalTime.NOON, 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            calculator.calculate(LocalDate.of(2026, 7, 25), LocalTime.NOON, -5)
        }
    }

    @Test
    fun `future check rejects current and past instants`() {
        val now = Instant.parse("2026-07-25T07:00:00Z")

        assertTrue(calculator.isFuture(now.plusSeconds(1), now))
        assertFalse(calculator.isFuture(now, now))
        assertFalse(calculator.isFuture(now.minusSeconds(1), now))
    }
}

package io.github.zapolyarnydev.ptktimetable.ui.schedule

import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Group
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.ScheduleMode
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekFilter
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType
import io.github.zapolyarnydev.ptktimetable.domain.schedule.service.WeekRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class SchedulePresentationLogicTest {

    @Test
    fun `week filter keeps common lessons and matching week`() {
        assertTrue(WeekRules.matches(WeekType.ALL, WeekFilter.UPPER))
        assertTrue(WeekRules.matches(WeekType.UPPER, WeekFilter.UPPER))
        assertFalse(WeekRules.matches(WeekType.LOWER, WeekFilter.UPPER))
        assertTrue(WeekRules.matches(WeekType.LOWER, WeekFilter.ALL))
    }

    @Test
    fun `day mode filters by selected day and week then sorts by time`() {
        val state = ScheduleUiState(
            mode = ScheduleMode.BY_DAY,
            selectedDay = ScheduleDay.TUESDAY,
            weekFilter = WeekFilter.UPPER,
            lessons = listOf(
                lesson(ScheduleDay.TUESDAY, "12.45-14.25", WeekType.UPPER, "Second"),
                lesson(ScheduleDay.MONDAY, "8.30-10.10", WeekType.UPPER, "Wrong day"),
                lesson(ScheduleDay.TUESDAY, "10.20-12.00", WeekType.LOWER, "Wrong week"),
                lesson(ScheduleDay.TUESDAY, "8.30-10.10", WeekType.ALL, "First"),
            ),
        )

        assertEquals(listOf("First", "Second"), ScheduleRules.visibleLessons(state).map { it.subject })
    }

    @Test
    fun `date mode keeps resolved lessons and sorts by time`() {
        val state = ScheduleUiState(
            mode = ScheduleMode.BY_DATE,
            selectedDay = ScheduleDay.MONDAY,
            weekFilter = WeekFilter.LOWER,
            lessons = listOf(
                lesson(ScheduleDay.FRIDAY, "12.45-14.25", WeekType.UPPER, "Second"),
                lesson(ScheduleDay.TUESDAY, "8.30-10.10", WeekType.ALL, "First"),
            ),
        )

        assertEquals(listOf("First", "Second"), ScheduleRules.visibleLessons(state).map { it.subject })
    }

    @Test
    fun `time slots preserve week halves`() {
        val slots = buildTimeSlots(
            listOf(
                lesson(ScheduleDay.MONDAY, "10.20-12.00", WeekType.LOWER, "Lower"),
                lesson(ScheduleDay.MONDAY, "8.30-10.10", WeekType.ALL, "Common"),
                lesson(ScheduleDay.MONDAY, "10.20-12.00", WeekType.UPPER, "Upper"),
            ),
        )

        assertEquals(listOf("8.30-10.10", "10.20-12.00"), slots.map { it.timeRange })
        assertEquals("Common", slots[0].allLessons.single().subject)
        assertEquals("Upper", slots[1].upperLessons.single().subject)
        assertEquals("Lower", slots[1].lowerLessons.single().subject)
    }

    @Test
    fun `date and day guards reject unrelated lesson slots`() {
        val today = LocalDate.now()

        assertEquals(
            null,
            ScheduleRules.currentLesson(
                lessons = listOf(lesson(ScheduleDay.MONDAY, "00.00-23.59", WeekType.ALL, "Past")),
                date = today.minusDays(1),
                selectedDay = dayOfWeekToScheduleDay(today.dayOfWeek),
                isDateMode = true,
                now = today.atTime(12, 0),
            ),
        )
        assertEquals(
            null,
            ScheduleRules.nextLesson(
                lessons = listOf(lesson(ScheduleDay.MONDAY, "23.59-23.59", WeekType.ALL, "Past")),
                date = today.minusDays(1),
                selectedDay = dayOfWeekToScheduleDay(today.dayOfWeek),
                isDateMode = true,
                now = today.atTime(12, 0),
            ),
        )
    }

    @Test
    fun `day mapping and week mismatch rules stay explicit`() {
        assertEquals(ScheduleDay.MONDAY, dayOfWeekToScheduleDay(DayOfWeek.MONDAY))
        assertEquals(ScheduleDay.SUNDAY, dayOfWeekToScheduleDay(DayOfWeek.SUNDAY))
        assertTrue(ScheduleRules.isWeekMismatch(WeekFilter.LOWER, WeekType.UPPER))
        assertFalse(ScheduleRules.isWeekMismatch(WeekFilter.ALL, WeekType.UPPER))
    }

    @Test
    fun `restored group lookup ignores case and surrounding spaces`() {
        val groups = listOf(
            Group("College", 1, "Course", "ISP-1", "first.xls"),
            Group("College", 2, "Course", "ISP-2", "second.xls"),
        )

        assertEquals("ISP-2", findRestoredGroup(groups, "  isp-2  ")?.groupName)
        assertEquals(null, findRestoredGroup(groups, "missing"))
    }

    private fun lesson(day: ScheduleDay, timeRange: String, weekType: WeekType, subject: String) = ScheduleLessonItem(
        day = day,
        dayLabel = day.title,
        startTime = parseTime(timeRange.substringBefore("-")),
        endTime = parseTime(timeRange.substringAfter("-")),
        weekType = weekType,
        subject = subject,
        teacher = null,
        classroom = null,
        rawText = subject,
    )

    private fun parseTime(value: String) =
        java.time.LocalTime.parse(value, java.time.format.DateTimeFormatter.ofPattern("H.mm"))
}

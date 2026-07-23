package io.github.zapolyarnydev.ptktimetable.ui.schedule

import io.github.zapolyarnydev.ptktimetable.data.model.PtkCurrentWeekType
import io.github.zapolyarnydev.ptktimetable.data.model.PtkGroupInfo
import io.github.zapolyarnydev.ptktimetable.data.model.PtkWeekType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class SchedulePresentationLogicTest {

    @Test
    fun `week filter keeps common lessons and matching week`() {
        assertTrue(lessonMatchesWeekFilter(PtkWeekType.ALL, ScheduleWeekFilter.UPPER))
        assertTrue(lessonMatchesWeekFilter(PtkWeekType.UPPER, ScheduleWeekFilter.UPPER))
        assertFalse(lessonMatchesWeekFilter(PtkWeekType.LOWER, ScheduleWeekFilter.UPPER))
        assertTrue(lessonMatchesWeekFilter(PtkWeekType.LOWER, ScheduleWeekFilter.ALL))
    }

    @Test
    fun `day mode filters by selected day and week then sorts by time`() {
        val state = ScheduleUiState(
            mode = ScheduleMode.BY_DAY,
            selectedDay = ScheduleDay.TUESDAY,
            weekFilter = ScheduleWeekFilter.UPPER,
            lessons = listOf(
                lesson(ScheduleDay.TUESDAY, "12.45-14.25", PtkWeekType.UPPER, "Second"),
                lesson(ScheduleDay.MONDAY, "8.30-10.10", PtkWeekType.UPPER, "Wrong day"),
                lesson(ScheduleDay.TUESDAY, "10.20-12.00", PtkWeekType.LOWER, "Wrong week"),
                lesson(ScheduleDay.TUESDAY, "8.30-10.10", PtkWeekType.ALL, "First"),
            ),
        )

        assertEquals(listOf("First", "Second"), filterLessons(state).map { it.subject })
    }

    @Test
    fun `date mode keeps resolved lessons and sorts by time`() {
        val state = ScheduleUiState(
            mode = ScheduleMode.BY_DATE,
            selectedDay = ScheduleDay.MONDAY,
            weekFilter = ScheduleWeekFilter.LOWER,
            lessons = listOf(
                lesson(ScheduleDay.FRIDAY, "12.45-14.25", PtkWeekType.UPPER, "Second"),
                lesson(ScheduleDay.TUESDAY, "8.30-10.10", PtkWeekType.ALL, "First"),
            ),
        )

        assertEquals(listOf("First", "Second"), filterLessons(state).map { it.subject })
    }

    @Test
    fun `time slots preserve week halves`() {
        val slots = buildTimeSlots(
            listOf(
                lesson(ScheduleDay.MONDAY, "10.20-12.00", PtkWeekType.LOWER, "Lower"),
                lesson(ScheduleDay.MONDAY, "8.30-10.10", PtkWeekType.ALL, "Common"),
                lesson(ScheduleDay.MONDAY, "10.20-12.00", PtkWeekType.UPPER, "Upper"),
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

        assertFalse(
            isCurrentLessonSlot(
                date = today.minusDays(1),
                selectedDay = dayOfWeekToScheduleDay(today.dayOfWeek),
                isDateMode = true,
                timeRange = "00.00-23.59",
            ),
        )
        assertFalse(
            isFutureLessonSlot(
                date = today.minusDays(1),
                selectedDay = dayOfWeekToScheduleDay(today.dayOfWeek),
                isDateMode = true,
                timeRange = "23.59-23.59",
            ),
        )
    }

    @Test
    fun `day mapping and week mismatch rules stay explicit`() {
        assertEquals(ScheduleDay.MONDAY, dayOfWeekToScheduleDay(DayOfWeek.MONDAY))
        assertEquals(ScheduleDay.SUNDAY, dayOfWeekToScheduleDay(DayOfWeek.SUNDAY))
        assertTrue(isWeekMismatchWarningNeeded(ScheduleWeekFilter.LOWER, PtkCurrentWeekType.UPPER))
        assertFalse(isWeekMismatchWarningNeeded(ScheduleWeekFilter.ALL, PtkCurrentWeekType.UPPER))
    }

    @Test
    fun `restored group lookup ignores case and surrounding spaces`() {
        val groups = listOf(
            PtkGroupInfo("College", 1, "Course", "ISP-1", "first.xls"),
            PtkGroupInfo("College", 2, "Course", "ISP-2", "second.xls"),
        )

        assertEquals("ISP-2", findRestoredGroup(groups, "  isp-2  ")?.groupName)
        assertEquals(null, findRestoredGroup(groups, "missing"))
    }

    private fun lesson(day: ScheduleDay, timeRange: String, weekType: PtkWeekType, subject: String) =
        ScheduleLessonItem(
            day = day,
            dayLabel = day.title,
            timeRange = timeRange,
            weekType = weekType,
            subject = subject,
            teacher = null,
            classroom = null,
            rawText = subject,
        )
}

package io.github.zapolyarnydev.ptktimetable.ui.schedule

import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.ScheduleMode
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekFilter
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType
import io.github.zapolyarnydev.ptktimetable.domain.schedule.service.WeekRules
import java.time.LocalDate
import java.time.LocalDateTime

internal object ScheduleRules {

    fun visibleLessons(state: ScheduleUiState): List<ScheduleLessonItem> {
        if (state.mode == ScheduleMode.BY_DATE) return state.lessons.sortedBy { it.startTime }
        val selectedDay = state.selectedDay ?: return emptyList()
        return state.lessons
            .filter { it.day == selectedDay }
            .filter { WeekRules.matches(it.weekType, state.weekFilter) }
            .sortedBy { it.startTime }
    }

    fun currentLesson(
        lessons: List<ScheduleLessonItem>,
        date: LocalDate,
        selectedDay: ScheduleDay?,
        isDateMode: Boolean,
        now: LocalDateTime = LocalDateTime.now(),
    ): ScheduleLessonItem? {
        if (!matchesSelectedDate(date, selectedDay, isDateMode, now.toLocalDate())) return null
        return lessons.firstOrNull { lesson ->
            val start = LocalDateTime.of(now.toLocalDate(), lesson.startTime)
            val end = LocalDateTime.of(now.toLocalDate(), lesson.endTime)
            !now.isBefore(start) && now.isBefore(end)
        }
    }

    fun nextLesson(
        lessons: List<ScheduleLessonItem>,
        date: LocalDate,
        selectedDay: ScheduleDay?,
        isDateMode: Boolean,
        now: LocalDateTime = LocalDateTime.now(),
    ): ScheduleLessonItem? {
        val targetDate = if (isDateMode) {
            date
        } else {
            if (selectedDay != dayOfWeekToScheduleDay(now.dayOfWeek)) return null
            now.toLocalDate()
        }
        return lessons
            .filter { LocalDateTime.of(targetDate, it.startTime).isAfter(now) }
            .minByOrNull { it.startTime }
    }

    fun isEditable(date: LocalDate, lesson: ScheduleLessonItem, now: LocalDateTime = LocalDateTime.now()): Boolean =
        !LocalDateTime.of(date, lesson.startTime).isBefore(now)

    fun isWeekMismatch(filter: WeekFilter, currentWeekType: WeekType?): Boolean {
        if (filter == WeekFilter.ALL) return false
        return when (currentWeekType) {
            WeekType.UPPER -> filter == WeekFilter.LOWER
            WeekType.LOWER -> filter == WeekFilter.UPPER
            WeekType.ALL, null -> false
        }
    }

    private fun matchesSelectedDate(
        date: LocalDate,
        selectedDay: ScheduleDay?,
        isDateMode: Boolean,
        today: LocalDate,
    ): Boolean = if (isDateMode) {
        date == today
    } else {
        selectedDay == dayOfWeekToScheduleDay(today.dayOfWeek)
    }
}

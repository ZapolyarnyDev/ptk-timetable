package io.github.zapolyarnydev.ptktimetable.feature.schedule.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.ScheduleMode
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekFilter
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleDay
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleLessonItem
import io.github.zapolyarnydev.ptktimetable.ui.schedule.TimeSlotUi
import io.github.zapolyarnydev.ptktimetable.ui.theme.PtkTheme
import java.time.LocalDate
import java.time.LocalTime

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun WeekSelectorPreview() {
    PtkTheme {
        Column(Modifier.padding(16.dp)) {
            WeekSelector(selected = WeekFilter.UPPER, onSelect = {})
        }
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun NavigatorPreview() {
    PtkTheme {
        Column(Modifier.padding(16.dp)) {
            DayNavigatorPanel(
                mode = ScheduleMode.BY_DAY,
                selectedDayTitle = ScheduleDay.MONDAY.title,
                selectedDate = LocalDate.of(2026, 9, 7),
                currentWeekType = WeekType.UPPER,
                weekMismatch = false,
                dayIndex = 0,
                totalDays = 6,
                canGoPrev = false,
                canGoNext = true,
                onSelectMode = {},
                onPreviousDay = {},
                onNextDay = {},
                onSelectDate = {},
                onPreviousDate = {},
                onNextDate = {},
                onGoToToday = {},
                availableDays = ScheduleDay.entries.filter { it != ScheduleDay.UNKNOWN },
                selectedDay = ScheduleDay.MONDAY,
                weekFilter = WeekFilter.UPPER,
                onSelectDay = {},
                onSelectWeekFilter = {},
                groupTitle = "Группа 3991",
                courseTitle = "4 курс",
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun LessonTablePreview() {
    val lesson = ScheduleLessonItem(
        day = ScheduleDay.MONDAY,
        dayLabel = ScheduleDay.MONDAY.title,
        startTime = LocalTime.of(9, 0),
        endTime = LocalTime.of(10, 30),
        weekType = WeekType.ALL,
        subject = "Разработка мобильных приложений",
        teacher = "Иванов И. И.",
        classroom = "312",
        rawText = "Разработка мобильных приложений",
    )
    PtkTheme {
        Column(Modifier.padding(16.dp)) {
            LessonTableCard(
                timeSlots = listOf(
                    TimeSlotUi(
                        startTime = lesson.startTime,
                        endTime = lesson.endTime,
                        allLessons = listOf(lesson),
                        upperLessons = emptyList(),
                        lowerLessons = emptyList(),
                    ),
                ),
                currentWeekType = WeekType.UPPER,
                weekFilter = WeekFilter.ALL,
                date = LocalDate.of(2026, 9, 7),
                isDateMode = true,
                currentLesson = lesson,
                nextLesson = null,
                noteMap = emptyMap(),
                reminderMap = emptyMap(),
                onAddOrEditNote = {},
                onAddOrEditReminder = {},
            )
        }
    }
}

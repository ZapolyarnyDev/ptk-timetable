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
import io.github.zapolyarnydev.ptktimetable.ui.schedule.buildTimeSlots
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
        subject = "Разработка мобильных приложений и распределённых пользовательских интерфейсов",
        teacher = "Иванов Иван Иванович, Петрова Анна Сергеевна",
        classroom = null,
        rawText = "Разработка мобильных приложений и распределённых пользовательских интерфейсов",
    )
    val upperLesson = lesson.copy(
        startTime = LocalTime.of(10, 40),
        endTime = LocalTime.of(12, 10),
        weekType = WeekType.UPPER,
        subject = "Архитектура программных систем",
        rawText = "Архитектура программных систем",
    )
    val lowerLesson = upperLesson.copy(
        weekType = WeekType.LOWER,
        subject = "Проектирование информационных систем",
        rawText = "Проектирование информационных систем",
    )
    PtkTheme {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 16.dp)) {
            LessonList(
                timeSlots = buildTimeSlots(
                    lessons = listOf(lesson, upperLesson, lowerLesson),
                    currentWeekType = WeekType.UPPER,
                    currentLesson = lesson,
                ),
                date = LocalDate.of(2026, 9, 7),
                isDateMode = true,
                noteMap = emptyMap(),
                reminderMap = emptyMap(),
                onAddOrEditNote = {},
                onAddOrEditReminder = {},
            )
        }
    }
}

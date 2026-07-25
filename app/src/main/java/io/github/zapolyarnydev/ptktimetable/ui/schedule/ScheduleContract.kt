package io.github.zapolyarnydev.ptktimetable.ui.schedule

import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Group
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.ScheduleMode
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekFilter
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class ScheduleDay(val title: String, val shortTitle: String, val order: Int) {
    MONDAY("Понедельник", "Пн", 1),
    TUESDAY("Вторник", "Вт", 2),
    WEDNESDAY("Среда", "Ср", 3),
    THURSDAY("Четверг", "Чт", 4),
    FRIDAY("Пятница", "Пт", 5),
    SATURDAY("Суббота", "Сб", 6),
    SUNDAY("Воскресенье", "Вс", 7),
    UNKNOWN("Другое", "?", 99),
}

data class ScheduleLessonItem(
    val day: ScheduleDay,
    val dayLabel: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val weekType: WeekType,
    val subject: String,
    val teacher: String?,
    val classroom: String?,
    val rawText: String,
) {
    val timeRange: String get() = "${TIME_FORMATTER.format(startTime)}-${TIME_FORMATTER.format(endTime)}"

    private companion object {
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("H.mm")
    }
}

data class TimeSlotUi(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val allLessons: List<ScheduleLessonItem>,
    val upperLessons: List<ScheduleLessonItem>,
    val lowerLessons: List<ScheduleLessonItem>,
) {
    val isSplitByWeek: Boolean get() = upperLessons.isNotEmpty() || lowerLessons.isNotEmpty()
    val lessons: List<ScheduleLessonItem> get() = allLessons + upperLessons + lowerLessons
    val timeRange: String get() = "${formatTime(startTime)}-${formatTime(endTime)}"
}

data class ScheduleDataPresentation(
    val visibleLessons: List<ScheduleLessonItem> = emptyList(),
    val timeSlots: List<TimeSlotUi> = emptyList(),
    val currentLesson: ScheduleLessonItem? = null,
    val nextLesson: ScheduleLessonItem? = null,
)

data class ScheduleDataState(
    val selectedGroup: Group?,
    val lessons: List<ScheduleLessonItem>,
    val availableDays: List<ScheduleDay>,
    val presentation: ScheduleDataPresentation,
    val isInitialLoading: Boolean,
    val isRefreshing: Boolean,
    val syncError: String?,
    val isOffline: Boolean,
    val updatedAt: Instant?,
)

data class ScheduleDateNavigationState(
    val mode: ScheduleMode,
    val selectedDate: LocalDate,
    val selectedDay: ScheduleDay?,
    val weekFilter: WeekFilter,
    val currentWeekType: WeekType?,
    val selectedDateWeekType: WeekType?,
    val dayIndex: Int,
    val totalDays: Int,
    val canGoPrevious: Boolean,
    val canGoNext: Boolean,
    val weekMismatch: Boolean,
)

data class ScheduleUiState(
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val syncError: String? = null,
    val hasCachedData: Boolean = false,
    val isOffline: Boolean = false,
    val selectedGroup: Group? = null,
    val mode: ScheduleMode = ScheduleMode.BY_DAY,
    val selectedDate: LocalDate = LocalDate.now(),
    val lessons: List<ScheduleLessonItem> = emptyList(),
    val availableDays: List<ScheduleDay> = emptyList(),
    val selectedDay: ScheduleDay? = null,
    val weekFilter: WeekFilter = WeekFilter.ALL,
    val currentWeekType: WeekType? = null,
    val selectedDateWeekType: WeekType? = null,
    val scheduleUpdatedAt: Instant? = null,
    val errorMessage: String? = null,
    val presentation: ScheduleDataPresentation = ScheduleDataPresentation(),
) {
    val data: ScheduleDataState
        get() = ScheduleDataState(
            selectedGroup = selectedGroup,
            lessons = lessons,
            availableDays = availableDays,
            presentation = presentation,
            isInitialLoading = isInitialLoading,
            isRefreshing = isRefreshing,
            syncError = syncError,
            isOffline = isOffline,
            updatedAt = scheduleUpdatedAt,
        )

    val dateNavigation: ScheduleDateNavigationState
        get() {
            val index = availableDays.indexOf(selectedDay).takeIf { it >= 0 } ?: 0
            return ScheduleDateNavigationState(
                mode = mode,
                selectedDate = selectedDate,
                selectedDay = selectedDay,
                weekFilter = weekFilter,
                currentWeekType = currentWeekType,
                selectedDateWeekType = selectedDateWeekType,
                dayIndex = index,
                totalDays = availableDays.size,
                canGoPrevious = mode == ScheduleMode.BY_DATE || index > 0,
                canGoNext = mode == ScheduleMode.BY_DATE || index < availableDays.lastIndex,
                weekMismatch = ScheduleRules.isWeekMismatch(weekFilter, currentWeekType),
            )
        }
}

sealed interface ScheduleUiAction {
    data object Refresh : ScheduleUiAction

    data object Back : ScheduleUiAction

    data class SelectMode(val mode: ScheduleMode) : ScheduleUiAction

    data class SelectDay(val day: ScheduleDay) : ScheduleUiAction

    data object PreviousDay : ScheduleUiAction

    data object NextDay : ScheduleUiAction

    data class SelectDate(val date: LocalDate) : ScheduleUiAction

    data object PreviousDate : ScheduleUiAction

    data object NextDate : ScheduleUiAction

    data object Today : ScheduleUiAction

    data class SelectWeekFilter(val filter: WeekFilter) : ScheduleUiAction
}

sealed interface ScheduleUiEvent {
    data object NavigateBack : ScheduleUiEvent
}

internal fun buildTimeSlots(lessons: List<ScheduleLessonItem>): List<TimeSlotUi> = lessons
    .groupBy { it.startTime to it.endTime }
    .map { (range, rows) ->
        TimeSlotUi(
            startTime = range.first,
            endTime = range.second,
            allLessons = rows.filter { it.weekType == WeekType.ALL },
            upperLessons = rows.filter { it.weekType == WeekType.UPPER },
            lowerLessons = rows.filter { it.weekType == WeekType.LOWER },
        )
    }
    .sortedBy { it.startTime }

private fun formatTime(value: LocalTime): String = DateTimeFormatter.ofPattern("H.mm").format(value)

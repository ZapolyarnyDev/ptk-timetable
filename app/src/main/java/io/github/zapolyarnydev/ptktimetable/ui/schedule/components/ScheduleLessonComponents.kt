package io.github.zapolyarnydev.ptktimetable.ui.schedule

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.zapolyarnydev.ptktimetable.data.model.PtkCurrentWeekType
import io.github.zapolyarnydev.ptktimetable.data.model.PtkWeekType
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppDimensions
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppIcons
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppShapes
import io.github.zapolyarnydev.ptktimetable.ui.theme.MaterialThemeAppColors
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun LessonTableCard(
    timeSlots: List<TimeSlotUi>,
    currentWeekType: PtkCurrentWeekType,
    weekFilter: ScheduleWeekFilter,
    date: LocalDate,
    selectedDay: ScheduleDay?,
    isDateMode: Boolean,
    noteMap: Map<String, ScheduleNoteItem>,
    reminderMap: Map<String, ScheduleNoteItem>,
    onAddOrEditNote: (ScheduleLessonItem) -> Unit,
    onAddOrEditReminder: (ScheduleLessonItem) -> Unit,
) {
    val nextTimeRange = timeSlots
        .filter { isFutureLessonSlot(date, selectedDay, isDateMode, it.timeRange) }
        .minByOrNull { lessonSortKey(it.timeRange) }
        ?.timeRange

    SectionCard(padding = 0.dp) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.primary))
            Text("Занятия", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "${timeSlots.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(AppShapes.pill)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            timeSlots.forEachIndexed { index, slot ->
                AnimatedReveal(key = "${slot.timeRange}-$index") {
                    LessonTableRow(
                        slot = slot,
                        currentWeekType = currentWeekType,
                        weekFilter = weekFilter,
                        date = date,
                        selectedDay = selectedDay,
                        isDateMode = isDateMode,
                        isNextSlot = slot.timeRange == nextTimeRange,
                        noteMap = noteMap,
                        reminderMap = reminderMap,
                        onAddOrEditNote = onAddOrEditNote,
                        onAddOrEditReminder = onAddOrEditReminder,
                    )
                }
            }
        }
    }
}

@Composable
internal fun LessonTableRow(
    slot: TimeSlotUi,
    currentWeekType: PtkCurrentWeekType,
    weekFilter: ScheduleWeekFilter,
    date: LocalDate,
    selectedDay: ScheduleDay?,
    isDateMode: Boolean,
    isNextSlot: Boolean,
    noteMap: Map<String, ScheduleNoteItem>,
    reminderMap: Map<String, ScheduleNoteItem>,
    onAddOrEditNote: (ScheduleLessonItem) -> Unit,
    onAddOrEditReminder: (ScheduleLessonItem) -> Unit,
) {
    val colors = MaterialThemeAppColors
    val (startTime, endTime) = splitTimeRange(slot.timeRange)
    val isCurrentSlot = isCurrentLessonSlot(date, selectedDay, isDateMode, slot.timeRange)
    val accent = when {
        isCurrentSlot -> colors.currentLesson
        isNextSlot -> colors.nextLesson
        else -> MaterialTheme.colorScheme.primary
    }
    val containerColor by animateColorAsState(
        targetValue = when {
            isCurrentSlot -> colors.currentLessonContainer
            isNextSlot -> colors.nextLessonContainer
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(220),
        label = "lessonContainerColor",
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isCurrentSlot || isNextSlot) 1.5.dp else 1.dp,
        animationSpec = tween(220),
        label = "lessonBorderWidth",
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = containerColor,
        shape = AppShapes.schedule,
        border = BorderStroke(
            borderWidth,
            if (isCurrentSlot || isNextSlot) {
                accent.copy(alpha = 0.58f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
        tonalElevation = if (isCurrentSlot) 2.dp else 0.dp,
    ) {
        Row(modifier = Modifier.fillMaxWidth().heightIn(min = 94.dp)) {
            Box(Modifier.width(5.dp).heightIn(min = 94.dp).background(accent))
            Column(
                modifier = Modifier
                    .width(AppDimensions.scheduleTimeColumn)
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    startTime,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                )
                Text(
                    endTime,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (isCurrentSlot || isNextSlot) {
                    Text(
                        text = if (isCurrentSlot) "СЕЙЧАС" else "ДАЛЬШЕ",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Box(Modifier.width(1.dp).heightIn(min = 94.dp).background(accent.copy(alpha = 0.2f)))
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                if (slot.isSplitByWeek) {
                    SplitWeekCell(
                        slot,
                        currentWeekType,
                        weekFilter,
                        date,
                        isDateMode,
                        noteMap,
                        reminderMap,
                        onAddOrEditNote,
                        onAddOrEditReminder,
                    )
                } else {
                    LessonTextBlock(
                        slot.allLessons,
                        currentWeekType,
                        date,
                        isDateMode,
                        noteMap,
                        reminderMap,
                        onAddOrEditNote,
                        onAddOrEditReminder,
                    )
                }
            }
        }
    }
}

@Composable
internal fun SplitWeekCell(
    slot: TimeSlotUi,
    currentWeekType: PtkCurrentWeekType,
    weekFilter: ScheduleWeekFilter,
    date: LocalDate,
    isDateMode: Boolean,
    noteMap: Map<String, ScheduleNoteItem>,
    reminderMap: Map<String, ScheduleNoteItem>,
    onAddOrEditNote: (ScheduleLessonItem) -> Unit,
    onAddOrEditReminder: (ScheduleLessonItem) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (slot.allLessons.isNotEmpty()) {
            LessonTextBlock(
                slot.allLessons,
                currentWeekType,
                date,
                isDateMode,
                noteMap,
                reminderMap,
                onAddOrEditNote,
                onAddOrEditReminder,
            )
        }
        val blocks = when {
            !isDateMode && weekFilter == ScheduleWeekFilter.UPPER -> listOf("Верхняя" to slot.upperLessons)
            !isDateMode && weekFilter == ScheduleWeekFilter.LOWER -> listOf("Нижняя" to slot.lowerLessons)
            else -> listOf("Верхняя" to slot.upperLessons, "Нижняя" to slot.lowerLessons)
        }
        blocks.forEachIndexed { index, (title, lessons) ->
            if (index > 0) DashedHorizontalDivider()
            WeekHalfBlock(
                title,
                lessons,
                if (title == "Верхняя") PtkWeekType.UPPER else PtkWeekType.LOWER,
                currentWeekType,
                date,
                isDateMode,
                noteMap,
                reminderMap,
                onAddOrEditNote,
                onAddOrEditReminder,
            )
        }
    }
}

@Composable
internal fun WeekHalfBlock(
    title: String,
    lessons: List<ScheduleLessonItem>,
    weekType: PtkWeekType,
    currentWeekType: PtkCurrentWeekType,
    date: LocalDate,
    isDateMode: Boolean,
    noteMap: Map<String, ScheduleNoteItem>,
    reminderMap: Map<String, ScheduleNoteItem>,
    onAddOrEditNote: (ScheduleLessonItem) -> Unit,
    onAddOrEditReminder: (ScheduleLessonItem) -> Unit,
) {
    val isCurrent = weekTypeMatchesCurrent(weekType, currentWeekType)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
        )
        if (isCurrent) {
            Text("сейчас", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
    if (lessons.isEmpty()) {
        Text(
            "Нет занятия",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        LessonTextBlock(
            lessons,
            currentWeekType,
            date,
            isDateMode,
            noteMap,
            reminderMap,
            onAddOrEditNote,
            onAddOrEditReminder,
        )
    }
}

@Composable
internal fun LessonTextBlock(
    lessons: List<ScheduleLessonItem>,
    currentWeekType: PtkCurrentWeekType,
    date: LocalDate,
    isDateMode: Boolean,
    noteMap: Map<String, ScheduleNoteItem>,
    reminderMap: Map<String, ScheduleNoteItem>,
    onAddOrEditNote: (ScheduleLessonItem) -> Unit,
    onAddOrEditReminder: (ScheduleLessonItem) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        lessons.forEachIndexed { index, lesson ->
            val isCurrent = weekTypeMatchesCurrent(lesson.weekType, currentWeekType)
            val note = noteMap[noteLessonKey(date, lesson.timeRange, lesson.weekType, lesson.subject, lesson.rawText)]
            val reminder = reminderMap[
                noteLessonKey(date, lesson.timeRange, lesson.weekType, lesson.subject, lesson.rawText),
            ]
            val details = listOfNotNull(
                lesson.teacher?.takeIf { it.isNotBlank() }?.let { AppIcons.person to it },
                lesson.classroom?.takeIf { it.isNotBlank() }?.let { AppIcons.room to it },
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            text = lesson.subject.ifBlank { lesson.rawText },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (lesson.weekType != PtkWeekType.ALL) {
                            Text(
                                text = weekTypeTitle(lesson.weekType),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCurrent) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        OutlinedIconActionButton(
                            icon = if (note != null) AppIcons.note else AppIcons.edit,
                            contentDescription = if (note != null) {
                                "Открыть заметку"
                            } else {
                                "Добавить заметку"
                            },
                            onClick = { onAddOrEditNote(lesson) },
                            enabled = isDateMode,
                            active = note != null,
                            size = 32.dp,
                        )
                        OutlinedIconActionButton(
                            icon = AppIcons.reminder,
                            contentDescription =
                            "Напоминание",
                            onClick = { onAddOrEditReminder(lesson) },
                            enabled = isDateMode,
                            active = reminder?.reminderEnabled == true,
                            size = 32.dp,
                        )
                    }
                }
                details.forEach { (icon, value) ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            value,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (note != null && note.noteText.isNotBlank()) {
                    Text(
                        text = "Заметка · ${note.noteText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                    )
                }
            }
            if (index < lessons.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

private fun weekTypeTitle(type: PtkWeekType): String = when (type) {
    PtkWeekType.ALL -> "Обе недели"
    PtkWeekType.UPPER -> "Верхняя неделя"
    PtkWeekType.LOWER -> "Нижняя неделя"
}

internal fun weekTypeMatchesCurrent(lessonWeekType: PtkWeekType, currentWeekType: PtkCurrentWeekType): Boolean =
    when (currentWeekType) {
        PtkCurrentWeekType.UNKNOWN -> true
        PtkCurrentWeekType.UPPER -> lessonWeekType == PtkWeekType.UPPER || lessonWeekType == PtkWeekType.ALL
        PtkCurrentWeekType.LOWER -> lessonWeekType == PtkWeekType.LOWER || lessonWeekType == PtkWeekType.ALL
    }

@Composable
internal fun DashedHorizontalDivider(color: Color = MaterialTheme.colorScheme.outlineVariant, stroke: Dp = 1.dp) {
    Box(
        modifier = Modifier.fillMaxWidth().height(2.dp).drawBehind {
            val y = size.height / 2f
            drawLine(
                color = color,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = stroke.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 5.dp.toPx())),
            )
        },
    )
}

data class TimeSlotUi(
    val timeRange: String,
    val allLessons: List<ScheduleLessonItem>,
    val upperLessons: List<ScheduleLessonItem>,
    val lowerLessons: List<ScheduleLessonItem>,
) {
    val isSplitByWeek: Boolean get() = upperLessons.isNotEmpty() || lowerLessons.isNotEmpty()
}

internal fun buildTimeSlots(lessons: List<ScheduleLessonItem>): List<TimeSlotUi> = lessons
    .groupBy { it.timeRange }
    .map { (timeRange, rows) ->
        TimeSlotUi(
            timeRange = timeRange,
            allLessons = rows.filter { it.weekType == PtkWeekType.ALL },
            upperLessons = rows.filter { it.weekType == PtkWeekType.UPPER },
            lowerLessons = rows.filter { it.weekType == PtkWeekType.LOWER },
        )
    }
    .sortedBy { lessonSortKey(it.timeRange) }

internal fun filterLessons(state: ScheduleUiState): List<ScheduleLessonItem> {
    if (state.mode == ScheduleMode.BY_DATE) return state.lessons.sortedBy { lessonSortKey(it.timeRange) }
    val selectedDay = state.selectedDay ?: return emptyList()
    return state.lessons
        .filter { it.day == selectedDay }
        .filter { lessonMatchesWeekFilter(it.weekType, state.weekFilter) }
        .sortedBy { lessonSortKey(it.timeRange) }
}

internal fun lessonMatchesWeekFilter(weekType: PtkWeekType, filter: ScheduleWeekFilter): Boolean = when (filter) {
    ScheduleWeekFilter.ALL -> true
    ScheduleWeekFilter.UPPER -> weekType == PtkWeekType.UPPER || weekType == PtkWeekType.ALL
    ScheduleWeekFilter.LOWER -> weekType == PtkWeekType.LOWER || weekType == PtkWeekType.ALL
}

internal fun lessonSortKey(timeRange: String): Int {
    val normalized = timeRange.replace('—', '-').replace('–', '-')
    val match = Regex("(\\d{1,2})[.:](\\d{2})").find(normalized) ?: return Int.MAX_VALUE
    return (match.groupValues[1].toIntOrNull() ?: 99) * 60 + (match.groupValues[2].toIntOrNull() ?: 99)
}

internal fun splitTimeRange(timeRange: String): Pair<String, String> {
    val normalized = timeRange.replace('—', '-').replace('–', '-').replace(" ", "")
    val parts = normalized.split("-", limit = 2)
    return parts.getOrNull(0).orEmpty().ifBlank { timeRange } to parts.getOrNull(1).orEmpty()
}

internal fun noteLessonKey(
    date: LocalDate,
    timeRange: String,
    weekType: PtkWeekType,
    subject: String,
    rawText: String,
): String = listOf(
    date.toString(),
    timeRange.trim(),
    weekType.name,
    subject.trim(),
    rawText.trim().hashCode().toString(),
).joinToString("|")

internal fun isLessonEditableNowOrFuture(date: LocalDate, timeRange: String): Boolean {
    val start = parseStartDateTime(date, timeRange) ?: return false
    return !start.isBefore(LocalDateTime.now())
}

internal fun isCurrentLessonSlot(
    date: LocalDate,
    selectedDay: ScheduleDay?,
    isDateMode: Boolean,
    timeRange: String,
): Boolean {
    val now = LocalDateTime.now()
    val isMatchingDay = if (isDateMode) {
        now.toLocalDate() == date
    } else {
        selectedDay == dayOfWeekToScheduleDay(now.dayOfWeek)
    }
    if (!isMatchingDay) return false
    val (startRaw, endRaw) = splitTimeRange(timeRange)
    val start = parseTime(now.toLocalDate(), startRaw) ?: return false
    val end = parseTime(now.toLocalDate(), endRaw) ?: return false
    return !now.isBefore(start) && now.isBefore(end)
}

internal fun isFutureLessonSlot(
    date: LocalDate,
    selectedDay: ScheduleDay?,
    isDateMode: Boolean,
    timeRange: String,
): Boolean {
    val today = LocalDate.now()
    val targetDate = if (isDateMode) {
        date
    } else {
        if (selectedDay != dayOfWeekToScheduleDay(today.dayOfWeek)) return false
        today
    }
    val start = parseStartDateTime(targetDate, timeRange) ?: return false
    return start.isAfter(LocalDateTime.now())
}

private fun parseStartDateTime(date: LocalDate, timeRange: String): LocalDateTime? =
    parseTime(date, splitTimeRange(timeRange).first)

private fun parseTime(date: LocalDate, text: String): LocalDateTime? {
    val match = Regex("(\\d{1,2})[.:](\\d{2})").find(text) ?: return null
    val hour = match.groupValues[1].toIntOrNull() ?: return null
    val minute = match.groupValues[2].toIntOrNull() ?: return null
    return runCatching { LocalDateTime.of(date, LocalTime.of(hour, minute)) }.getOrNull()
}

internal fun dayOfWeekToScheduleDay(dayOfWeek: java.time.DayOfWeek): ScheduleDay = when (dayOfWeek) {
    java.time.DayOfWeek.MONDAY -> ScheduleDay.MONDAY
    java.time.DayOfWeek.TUESDAY -> ScheduleDay.TUESDAY
    java.time.DayOfWeek.WEDNESDAY -> ScheduleDay.WEDNESDAY
    java.time.DayOfWeek.THURSDAY -> ScheduleDay.THURSDAY
    java.time.DayOfWeek.FRIDAY -> ScheduleDay.FRIDAY
    java.time.DayOfWeek.SATURDAY -> ScheduleDay.SATURDAY
    java.time.DayOfWeek.SUNDAY -> ScheduleDay.SUNDAY
}

internal fun formatDateTitle(date: LocalDate): String =
    DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("ru")).format(date)

internal fun weekTypeLabel(type: PtkCurrentWeekType): String = when (type) {
    PtkCurrentWeekType.UPPER -> "верхняя"
    PtkCurrentWeekType.LOWER -> "нижняя"
    PtkCurrentWeekType.UNKNOWN -> "не определена"
}

internal fun isWeekMismatchWarningNeeded(
    selectedFilter: ScheduleWeekFilter,
    currentWeekType: PtkCurrentWeekType,
): Boolean {
    if (selectedFilter == ScheduleWeekFilter.ALL) return false
    return when (currentWeekType) {
        PtkCurrentWeekType.UNKNOWN -> false
        PtkCurrentWeekType.UPPER -> selectedFilter == ScheduleWeekFilter.LOWER
        PtkCurrentWeekType.LOWER -> selectedFilter == ScheduleWeekFilter.UPPER
    }
}

internal fun formatInstant(value: java.time.Instant): String =
    DateTimeFormatter.ofPattern("dd.MM HH:mm", Locale.forLanguageTag("ru"))
        .withZone(java.time.ZoneId.systemDefault())
        .format(value)

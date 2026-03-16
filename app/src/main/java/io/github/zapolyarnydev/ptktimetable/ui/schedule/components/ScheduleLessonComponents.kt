package io.github.zapolyarnydev.ptktimetable.ui.schedule

import android.app.DatePickerDialog
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zapolyarnydev.ptktimetable.data.model.PtkCurrentWeekType
import io.github.zapolyarnydev.ptktimetable.data.model.PtkGroupInfo
import io.github.zapolyarnydev.ptktimetable.data.model.PtkWeekType
import io.github.zapolyarnydev.ptktimetable.ui.theme.BorderSubtle
import io.github.zapolyarnydev.ptktimetable.ui.theme.BorderStrong
import io.github.zapolyarnydev.ptktimetable.ui.theme.HeadingFontFamily
import io.github.zapolyarnydev.ptktimetable.ui.theme.InkPrimary
import io.github.zapolyarnydev.ptktimetable.ui.theme.InkMuted
import io.github.zapolyarnydev.ptktimetable.ui.theme.InkSecondary
import io.github.zapolyarnydev.ptktimetable.ui.theme.MainFontFamily
import io.github.zapolyarnydev.ptktimetable.ui.theme.NovsuBlue
import io.github.zapolyarnydev.ptktimetable.ui.theme.NovsuBlueDark
import io.github.zapolyarnydev.ptktimetable.ui.theme.NovsuBlueSoft
import io.github.zapolyarnydev.ptktimetable.ui.theme.SurfaceBlueTint
import io.github.zapolyarnydev.ptktimetable.ui.theme.SurfaceMuted
import io.github.zapolyarnydev.ptktimetable.ui.theme.White
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun LessonTableCard(
    timeSlots: List<TimeSlotUi>,
    currentWeekType: PtkCurrentWeekType,
    weekFilter: ScheduleWeekFilter,
    date: LocalDate,
    isDateMode: Boolean,
    noteMap: Map<String, ScheduleNoteItem>,
    reminderMap: Map<String, ScheduleNoteItem>,
    onAddOrEditNote: (ScheduleLessonItem) -> Unit,
    onAddOrEditReminder: (ScheduleLessonItem) -> Unit
) {
    SectionCard(padding = 0.dp) {
        timeSlots.forEachIndexed { index, slot ->
            LessonTableRow(
                slot = slot,
                currentWeekType = currentWeekType,
                weekFilter = weekFilter,
                date = date,
                isDateMode = isDateMode,
                noteMap = noteMap,
                reminderMap = reminderMap,
                onAddOrEditNote = onAddOrEditNote,
                onAddOrEditReminder = onAddOrEditReminder
            )
            if (index < timeSlots.lastIndex) {
                HorizontalDivider(thickness = 0.8.dp, color = BorderSubtle)
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
    isDateMode: Boolean,
    noteMap: Map<String, ScheduleNoteItem>,
    reminderMap: Map<String, ScheduleNoteItem>,
    onAddOrEditNote: (ScheduleLessonItem) -> Unit,
    onAddOrEditReminder: (ScheduleLessonItem) -> Unit
) {
    val (startTime, endTime) = splitTimeRange(slot.timeRange)
    val timeIndent = 22.dp
    val isCurrentSlot = isDateMode && isCurrentLessonSlot(date, slot.timeRange)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 82.dp)
            .background(if (isCurrentSlot) SurfaceBlueTint else Color.Transparent)
            .drawBehind {
                if (isCurrentSlot) {
                    drawLine(
                        color = NovsuBlueDark,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }
    ) {
        Column(
            modifier = Modifier
                .width(94.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = NovsuBlue,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = startTime,
                    style = MaterialTheme.typography.titleSmall,
                    color = InkPrimary,
                    fontFamily = HeadingFontFamily
                )
            }

            Row {
                Spacer(modifier = Modifier.width(timeIndent))
                Text(
                    text = endTime,
                    style = MaterialTheme.typography.labelLarge,
                    color = InkPrimary,
                    fontFamily = HeadingFontFamily
                )
            }
        }

        Box(
            modifier = Modifier
                .width(1.dp)
                .heightIn(min = 82.dp)
                .background(BorderStrong)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            if (slot.isSplitByWeek) {
                SplitWeekCell(
                    slot = slot,
                    currentWeekType = currentWeekType,
                    weekFilter = weekFilter,
                    date = date,
                    isDateMode = isDateMode,
                    noteMap = noteMap,
                    reminderMap = reminderMap,
                    onAddOrEditNote = onAddOrEditNote,
                    onAddOrEditReminder = onAddOrEditReminder
                )
            } else {
                LessonTextBlock(
                    lessons = slot.allLessons,
                    currentWeekType = currentWeekType,
                    date = date,
                    isDateMode = isDateMode,
                    noteMap = noteMap,
                    reminderMap = reminderMap,
                    onAddOrEditNote = onAddOrEditNote,
                    onAddOrEditReminder = onAddOrEditReminder
                )
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
    onAddOrEditReminder: (ScheduleLessonItem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        when {
            !isDateMode && weekFilter == ScheduleWeekFilter.UPPER -> {
                WeekHalfBlock(
                    title = "Верхняя",
                    lessons = slot.upperLessons,
                    weekType = PtkWeekType.UPPER,
                    currentWeekType = currentWeekType,
                    date = date,
                    isDateMode = isDateMode,
                    noteMap = noteMap,
                    reminderMap = reminderMap,
                    onAddOrEditNote = onAddOrEditNote,
                    onAddOrEditReminder = onAddOrEditReminder
                )
            }
            !isDateMode && weekFilter == ScheduleWeekFilter.LOWER -> {
                WeekHalfBlock(
                    title = "Нижняя",
                    lessons = slot.lowerLessons,
                    weekType = PtkWeekType.LOWER,
                    currentWeekType = currentWeekType,
                    date = date,
                    isDateMode = isDateMode,
                    noteMap = noteMap,
                    reminderMap = reminderMap,
                    onAddOrEditNote = onAddOrEditNote,
                    onAddOrEditReminder = onAddOrEditReminder
                )
            }
            else -> {
                WeekHalfBlock(
                    title = "Верхняя",
                    lessons = slot.upperLessons,
                    weekType = PtkWeekType.UPPER,
                    currentWeekType = currentWeekType,
                    date = date,
                    isDateMode = isDateMode,
                    noteMap = noteMap,
                    reminderMap = reminderMap,
                    onAddOrEditNote = onAddOrEditNote,
                    onAddOrEditReminder = onAddOrEditReminder
                )
                DashedHorizontalDivider()
                WeekHalfBlock(
                    title = "Нижняя",
                    lessons = slot.lowerLessons,
                    weekType = PtkWeekType.LOWER,
                    currentWeekType = currentWeekType,
                    date = date,
                    isDateMode = isDateMode,
                    noteMap = noteMap,
                    reminderMap = reminderMap,
                    onAddOrEditNote = onAddOrEditNote,
                    onAddOrEditReminder = onAddOrEditReminder
                )
            }
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
    onAddOrEditReminder: (ScheduleLessonItem) -> Unit
) {
    val isCurrent = weekTypeMatchesCurrent(weekType, currentWeekType)
    val titleAlpha = if (isCurrent) 1f else 0.9f

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = NovsuBlue.copy(alpha = titleAlpha),
            fontFamily = MainFontFamily,
            fontWeight = FontWeight.SemiBold
        )
        if (lessons.isEmpty()) {
            Text(
                text = "-",
                style = MaterialTheme.typography.bodyMedium,
                color = InkMuted.copy(alpha = titleAlpha)
            )
        } else {
            LessonTextBlock(
                lessons = lessons,
                currentWeekType = currentWeekType,
                date = date,
                isDateMode = isDateMode,
                noteMap = noteMap,
                reminderMap = reminderMap,
                onAddOrEditNote = onAddOrEditNote,
                onAddOrEditReminder = onAddOrEditReminder
            )
        }
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
    onAddOrEditReminder: (ScheduleLessonItem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lessons.forEachIndexed { index, lesson ->
            val isCurrent = weekTypeMatchesCurrent(lesson.weekType, currentWeekType)
            val textAlpha = if (isCurrent) 1f else 0.9f
            val note = noteMap[noteLessonKey(date, lesson.timeRange, lesson.weekType, lesson.subject, lesson.rawText)]
            val reminder = reminderMap[noteLessonKey(date, lesson.timeRange, lesson.weekType, lesson.subject, lesson.rawText)]
            val mainText = lesson.subject.ifBlank { lesson.rawText }
            val details = listOfNotNull(
                lesson.teacher?.takeIf { it.isNotBlank() },
                lesson.classroom?.takeIf { it.isNotBlank() }
            ).joinToString(", ")

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = mainText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = InkPrimary.copy(alpha = textAlpha),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onAddOrEditNote(lesson) },
                            enabled = isDateMode,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Notes,
                                contentDescription = "Заметка",
                                tint = if (note != null) NovsuBlue.copy(alpha = textAlpha) else InkSecondary.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { onAddOrEditReminder(lesson) },
                            enabled = isDateMode,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsActive,
                                contentDescription = "Напоминание",
                                tint = if (reminder?.reminderEnabled == true) NovsuBlue.copy(alpha = textAlpha) else InkSecondary.copy(alpha = 0.35f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                if (details.isNotBlank()) {
                    Text(
                        text = details,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary.copy(alpha = textAlpha)
                    )
                }
                if (note != null) {
                    Text(
                        text = "Заметка: ${note.noteText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NovsuBlueDark.copy(alpha = textAlpha),
                        maxLines = 2
                    )
                }
            }

            if (index < lessons.lastIndex) {
                HorizontalDivider(thickness = 0.8.dp, color = BorderSubtle.copy(alpha = 0.9f))
            }
        }
    }
}

internal fun weekTypeMatchesCurrent(
    lessonWeekType: PtkWeekType,
    currentWeekType: PtkCurrentWeekType
): Boolean {
    return when (currentWeekType) {
        PtkCurrentWeekType.UNKNOWN -> true
        PtkCurrentWeekType.UPPER -> {
            lessonWeekType == PtkWeekType.UPPER || lessonWeekType == PtkWeekType.ALL
        }
        PtkCurrentWeekType.LOWER -> {
            lessonWeekType == PtkWeekType.LOWER || lessonWeekType == PtkWeekType.ALL
        }
    }
}

@Composable
internal fun DashedHorizontalDivider(
    color: Color = BorderSubtle,
    stroke: Dp = 1.dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .drawBehind {
                val y = size.height / 2f
                drawLine(
                    color = color,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = stroke.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 6.dp.toPx()), 0f)
                )
            }
    )
}

data class TimeSlotUi(
    val timeRange: String,
    val allLessons: List<ScheduleLessonItem>,
    val upperLessons: List<ScheduleLessonItem>,
    val lowerLessons: List<ScheduleLessonItem>
) {
    val isSplitByWeek: Boolean
        get() = upperLessons.isNotEmpty() || lowerLessons.isNotEmpty()
}

internal fun buildTimeSlots(lessons: List<ScheduleLessonItem>): List<TimeSlotUi> {
    return lessons
        .groupBy { it.timeRange }
        .map { (timeRange, rows) ->
            TimeSlotUi(
                timeRange = timeRange,
                allLessons = rows.filter { it.weekType == PtkWeekType.ALL },
                upperLessons = rows.filter { it.weekType == PtkWeekType.UPPER },
                lowerLessons = rows.filter { it.weekType == PtkWeekType.LOWER }
            )
        }
        .sortedBy { lessonSortKey(it.timeRange) }
}

internal fun filterLessons(state: ScheduleUiState): List<ScheduleLessonItem> {
    if (state.mode == ScheduleMode.BY_DATE) {
        return state.lessons
            .asSequence()
            .sortedBy { lessonSortKey(it.timeRange) }
            .toList()
    }

    val selectedDay = state.selectedDay ?: return emptyList()
    return state.lessons
        .asSequence()
        .filter { it.day == selectedDay }
        .filter { lesson -> lessonMatchesWeekFilter(lesson.weekType, state.weekFilter) }
        .sortedBy { lessonSortKey(it.timeRange) }
        .toList()
}

internal fun lessonMatchesWeekFilter(weekType: PtkWeekType, filter: ScheduleWeekFilter): Boolean {
    return when (filter) {
        ScheduleWeekFilter.ALL -> true
        ScheduleWeekFilter.UPPER -> weekType == PtkWeekType.UPPER || weekType == PtkWeekType.ALL
        ScheduleWeekFilter.LOWER -> weekType == PtkWeekType.LOWER || weekType == PtkWeekType.ALL
    }
}

internal fun lessonSortKey(timeRange: String): Int {
    val normalized = timeRange.replace('—', '-').replace('–', '-')
    val match = Regex("(\\d{1,2})[.:](\\d{2})").find(normalized) ?: return Int.MAX_VALUE
    val hours = match.groupValues[1].toIntOrNull() ?: return Int.MAX_VALUE
    val minutes = match.groupValues[2].toIntOrNull() ?: return Int.MAX_VALUE
    return hours * 60 + minutes
}

internal fun splitTimeRange(timeRange: String): Pair<String, String> {
    val normalized = timeRange
        .replace('—', '-')
        .replace('–', '-')
        .replace(" ", "")
    val parts = normalized.split("-", limit = 2)
    val start = parts.getOrNull(0).orEmpty().ifBlank { timeRange }
    val end = parts.getOrNull(1).orEmpty().ifBlank { "" }
    return start to end
}

internal fun noteLessonKey(
    date: LocalDate,
    timeRange: String,
    weekType: PtkWeekType,
    subject: String,
    rawText: String
): String {
    return listOf(
        date.toString(),
        timeRange.trim(),
        weekType.name,
        subject.trim(),
        rawText.trim().hashCode().toString()
    ).joinToString("|")
}

internal fun isLessonEditableNowOrFuture(
    date: LocalDate,
    timeRange: String
): Boolean {
    val start = splitTimeRange(timeRange).first
    val match = Regex("(\\d{1,2})[.:](\\d{2})").find(start) ?: return false
    val h = match.groupValues[1].toIntOrNull() ?: return false
    val m = match.groupValues[2].toIntOrNull() ?: return false
    val startDateTime = runCatching { java.time.LocalDateTime.of(date, java.time.LocalTime.of(h, m)) }.getOrNull()
        ?: return false
    return !startDateTime.isBefore(java.time.LocalDateTime.now())
}

internal fun isCurrentLessonSlot(
    date: LocalDate,
    timeRange: String
): Boolean {
    val now = java.time.LocalDateTime.now()
    if (now.toLocalDate() != date) return false
    val (startRaw, endRaw) = splitTimeRange(timeRange)
    val startMatch = Regex("(\\d{1,2})[.:](\\d{2})").find(startRaw) ?: return false
    val endMatch = Regex("(\\d{1,2})[.:](\\d{2})").find(endRaw) ?: return false
    val start = runCatching {
        java.time.LocalDateTime.of(
            date,
            java.time.LocalTime.of(startMatch.groupValues[1].toInt(), startMatch.groupValues[2].toInt())
        )
    }.getOrNull() ?: return false
    val end = runCatching {
        java.time.LocalDateTime.of(
            date,
            java.time.LocalTime.of(endMatch.groupValues[1].toInt(), endMatch.groupValues[2].toInt())
        )
    }.getOrNull() ?: return false
    return !now.isBefore(start) && now.isBefore(end)
}

internal fun formatDateTitle(date: LocalDate): String {
    return DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.forLanguageTag("ru"))
        .format(date)
}

internal fun weekTypeLabel(type: PtkCurrentWeekType): String = when (type) {
    PtkCurrentWeekType.UPPER -> "верхняя"
    PtkCurrentWeekType.LOWER -> "нижняя"
    PtkCurrentWeekType.UNKNOWN -> "не определена"
}

internal fun isWeekMismatchWarningNeeded(
    selectedFilter: ScheduleWeekFilter,
    currentWeekType: PtkCurrentWeekType
): Boolean {
    if (selectedFilter == ScheduleWeekFilter.ALL) return false
    return when (currentWeekType) {
        PtkCurrentWeekType.UNKNOWN -> false
        PtkCurrentWeekType.UPPER -> selectedFilter == ScheduleWeekFilter.LOWER
        PtkCurrentWeekType.LOWER -> selectedFilter == ScheduleWeekFilter.UPPER
    }
}

internal fun formatInstant(value: Instant): String {
    return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru"))
        .withZone(ZoneId.systemDefault())
        .format(value)
}

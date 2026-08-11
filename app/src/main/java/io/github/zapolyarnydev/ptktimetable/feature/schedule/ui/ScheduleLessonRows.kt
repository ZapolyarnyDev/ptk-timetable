package io.github.zapolyarnydev.ptktimetable.feature.schedule.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.zapolyarnydev.ptktimetable.core.ui.BorderlessIconActionButton
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType
import io.github.zapolyarnydev.ptktimetable.feature.notes.ScheduleNoteItem
import io.github.zapolyarnydev.ptktimetable.feature.notes.noteLessonKey
import io.github.zapolyarnydev.ptktimetable.ui.schedule.LessonRowUi
import io.github.zapolyarnydev.ptktimetable.ui.schedule.LessonSlotStatus
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleLessonItem
import io.github.zapolyarnydev.ptktimetable.ui.schedule.TimeSlotUi
import io.github.zapolyarnydev.ptktimetable.ui.schedule.WeekSectionUi
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppDimensions
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppIcons
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppShapes
import io.github.zapolyarnydev.ptktimetable.ui.theme.MaterialThemeAppColors
import java.time.LocalDate

@Composable
internal fun LessonTableRow(
    slot: TimeSlotUi,
    date: LocalDate,
    isDateMode: Boolean,
    noteMap: Map<String, ScheduleNoteItem>,
    reminderMap: Map<String, ScheduleNoteItem>,
    onAddOrEditNote: (ScheduleLessonItem) -> Unit,
    onAddOrEditReminder: (ScheduleLessonItem) -> Unit,
) {
    val colors = MaterialThemeAppColors
    val containerColor by animateColorAsState(
        targetValue = when (slot.status) {
            LessonSlotStatus.CURRENT -> colors.currentLesson
            LessonSlotStatus.NEXT -> colors.surfaceMuted.copy(alpha = 0.72f)
            LessonSlotStatus.DEFAULT -> colors.background.copy(alpha = 0f)
        },
        animationSpec = tween(220),
        label = "lessonContainerColor",
    )
    val accentColor = when (slot.status) {
        LessonSlotStatus.CURRENT -> colors.accent
        LessonSlotStatus.NEXT -> colors.accent.copy(alpha = 0.38f)
        LessonSlotStatus.DEFAULT -> colors.background.copy(alpha = 0f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(AppShapes.small)
            .background(containerColor)
            .heightIn(min = 88.dp),
    ) {
        Box(Modifier.fillMaxHeight().width(3.dp).background(accentColor))
        Column(
            modifier = Modifier
                .width(AppDimensions.scheduleTimeColumn)
                .padding(start = 10.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = slot.startTimeLabel,
                style = MaterialTheme.typography.titleSmall,
                color = if (slot.status == LessonSlotStatus.DEFAULT) colors.textPrimary else colors.accent,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = slot.endTimeLabel,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
            when (slot.status) {
                LessonSlotStatus.CURRENT -> SlotStatusLabel("Сейчас")
                LessonSlotStatus.NEXT -> SlotStatusLabel("Дальше")
                LessonSlotStatus.DEFAULT -> Unit
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = 6.dp,
                    end = AppDimensions.scheduleContentPadding,
                    top = 14.dp,
                    bottom = 14.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (slot.commonRows.isNotEmpty()) {
                LessonTextBlock(
                    rows = slot.commonRows,
                    date = date,
                    isDateMode = isDateMode,
                    noteMap = noteMap,
                    reminderMap = reminderMap,
                    onAddOrEditNote = onAddOrEditNote,
                    onAddOrEditReminder = onAddOrEditReminder,
                )
            }
            slot.weekSections.forEachIndexed { index, section ->
                if (slot.commonRows.isNotEmpty() || index > 0) {
                    HorizontalDivider(color = colors.divider.copy(alpha = 0.45f))
                }
                WeekSection(
                    section = section,
                    date = date,
                    isDateMode = isDateMode,
                    noteMap = noteMap,
                    reminderMap = reminderMap,
                    onAddOrEditNote = onAddOrEditNote,
                    onAddOrEditReminder = onAddOrEditReminder,
                )
            }
        }
    }
}

@Composable
private fun SlotStatusLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialThemeAppColors.accent,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun WeekSection(
    section: WeekSectionUi,
    date: LocalDate,
    isDateMode: Boolean,
    noteMap: Map<String, ScheduleNoteItem>,
    reminderMap: Map<String, ScheduleNoteItem>,
    onAddOrEditNote: (ScheduleLessonItem) -> Unit,
    onAddOrEditReminder: (ScheduleLessonItem) -> Unit,
) {
    val colors = MaterialThemeAppColors
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.labelLarge,
                color = if (section.isCurrentWeek) colors.accent else colors.textSecondary,
                fontWeight = if (section.isCurrentWeek) FontWeight.Bold else FontWeight.Medium,
            )
            if (section.isCurrentWeek) {
                Text(
                    text = "сейчас",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.accent,
                )
            }
        }
        LessonTextBlock(
            rows = section.rows,
            date = date,
            isDateMode = isDateMode,
            noteMap = noteMap,
            reminderMap = reminderMap,
            onAddOrEditNote = onAddOrEditNote,
            onAddOrEditReminder = onAddOrEditReminder,
        )
    }
}

@Composable
private fun LessonTextBlock(
    rows: List<LessonRowUi>,
    date: LocalDate,
    isDateMode: Boolean,
    noteMap: Map<String, ScheduleNoteItem>,
    reminderMap: Map<String, ScheduleNoteItem>,
    onAddOrEditNote: (ScheduleLessonItem) -> Unit,
    onAddOrEditReminder: (ScheduleLessonItem) -> Unit,
) {
    val colors = MaterialThemeAppColors
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        rows.forEachIndexed { index, row ->
            val lesson = row.lesson
            val lessonKey = noteLessonKey(
                date,
                lesson.timeRange,
                lesson.weekType,
                lesson.subject,
                lesson.rawText,
            )
            val note = noteMap[lessonKey]
            val reminder = reminderMap[lessonKey]

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = lesson.subject.ifBlank { lesson.rawText },
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                )
                lesson.teacher?.takeIf { it.isNotBlank() }?.let {
                    LessonDetail(icon = AppIcons.person, text = it)
                }
                lesson.classroom?.takeIf { it.isNotBlank() }?.let {
                    LessonDetail(icon = AppIcons.room, text = it)
                }
                if (note != null && note.noteText.isNotBlank()) {
                    Text(
                        text = "Заметка · ${note.noteText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.accent,
                        maxLines = 2,
                    )
                }
                if (isDateMode) {
                    HorizontalDivider(color = colors.divider.copy(alpha = 0.28f))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 1.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BorderlessIconActionButton(
                            icon = if (note != null) AppIcons.note else AppIcons.edit,
                            contentDescription = if (note != null) "Открыть заметку" else "Добавить заметку",
                            onClick = { onAddOrEditNote(lesson) },
                            modifier = Modifier.testTag("lesson-note-action"),
                            active = note != null,
                        )
                        BorderlessIconActionButton(
                            icon = AppIcons.reminder,
                            contentDescription = if (reminder?.reminderEnabled == true) {
                                "Изменить напоминание"
                            } else {
                                "Добавить напоминание"
                            },
                            onClick = { onAddOrEditReminder(lesson) },
                            modifier = Modifier.testTag("lesson-reminder-action"),
                            active = reminder?.reminderEnabled == true,
                        )
                    }
                }
            }
            if (index < rows.lastIndex) {
                HorizontalDivider(color = colors.divider.copy(alpha = 0.45f))
            }
        }
    }
}

@Composable
private fun LessonDetail(icon: ImageVector, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialThemeAppColors.textSecondary,
            modifier = Modifier.padding(top = 2.dp).size(14.dp),
        )
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialThemeAppColors.textSecondary,
        )
    }
}

internal fun weekTypeLabel(type: WeekType?): String = when (type) {
    WeekType.UPPER -> "верхняя"
    WeekType.LOWER -> "нижняя"
    WeekType.ALL, null -> "не определена"
}

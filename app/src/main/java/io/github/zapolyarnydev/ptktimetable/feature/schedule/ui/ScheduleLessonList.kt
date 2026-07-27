package io.github.zapolyarnydev.ptktimetable.feature.schedule.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.zapolyarnydev.ptktimetable.core.ui.AnimatedReveal
import io.github.zapolyarnydev.ptktimetable.feature.notes.ScheduleNoteItem
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleLessonItem
import io.github.zapolyarnydev.ptktimetable.ui.schedule.TimeSlotUi
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppDimensions
import io.github.zapolyarnydev.ptktimetable.ui.theme.MaterialThemeAppColors
import java.time.LocalDate

@Composable
internal fun LessonList(
    timeSlots: List<TimeSlotUi>,
    date: LocalDate,
    isDateMode: Boolean,
    noteMap: Map<String, ScheduleNoteItem>,
    reminderMap: Map<String, ScheduleNoteItem>,
    onAddOrEditNote: (ScheduleLessonItem) -> Unit,
    onAddOrEditReminder: (ScheduleLessonItem) -> Unit,
) {
    val colors = MaterialThemeAppColors
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(
                horizontal = AppDimensions.scheduleContentPadding,
                vertical = AppDimensions.compactSpacing,
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Занятия",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = timeSlots.size.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
        }
        timeSlots.forEachIndexed { index, slot ->
            AnimatedReveal(key = slot.timeRange) {
                LessonTableRow(
                    slot = slot,
                    date = date,
                    isDateMode = isDateMode,
                    noteMap = noteMap,
                    reminderMap = reminderMap,
                    onAddOrEditNote = onAddOrEditNote,
                    onAddOrEditReminder = onAddOrEditReminder,
                )
            }
            if (index < timeSlots.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(
                        start = AppDimensions.scheduleTimeColumn + AppDimensions.scheduleContentPadding,
                        end = AppDimensions.scheduleContentPadding,
                    ),
                    color = colors.divider.copy(alpha = 0.5f),
                )
            }
        }
    }
}

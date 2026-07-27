package io.github.zapolyarnydev.ptktimetable.feature.schedule.ui

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
import io.github.zapolyarnydev.ptktimetable.core.ui.AnimatedReveal
import io.github.zapolyarnydev.ptktimetable.core.ui.OutlinedIconActionButton
import io.github.zapolyarnydev.ptktimetable.core.ui.SectionCard
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.ScheduleMode
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekFilter
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType
import io.github.zapolyarnydev.ptktimetable.domain.schedule.service.WeekRules
import io.github.zapolyarnydev.ptktimetable.feature.notes.ScheduleNoteItem
import io.github.zapolyarnydev.ptktimetable.feature.notes.noteLessonKey
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleDay
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleLessonItem
import io.github.zapolyarnydev.ptktimetable.ui.schedule.TimeSlotUi
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppDimensions
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppIcons
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppShapes
import io.github.zapolyarnydev.ptktimetable.ui.theme.MaterialThemeAppColors
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
internal fun LessonTableCard(
    timeSlots: List<TimeSlotUi>,
    currentWeekType: WeekType?,
    weekFilter: WeekFilter,
    date: LocalDate,
    isDateMode: Boolean,
    currentLesson: ScheduleLessonItem?,
    nextLesson: ScheduleLessonItem?,
    noteMap: Map<String, ScheduleNoteItem>,
    reminderMap: Map<String, ScheduleNoteItem>,
    onAddOrEditNote: (ScheduleLessonItem) -> Unit,
    onAddOrEditReminder: (ScheduleLessonItem) -> Unit,
) {
    val colors = MaterialThemeAppColors
    SectionCard(padding = 0.dp) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(colors.accent))
            Text("Занятия", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "${timeSlots.size}",
                style = MaterialTheme.typography.labelLarge,
                color = colors.accent,
                modifier = Modifier
                    .clip(AppShapes.pill)
                    .background(colors.accentMuted)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        HorizontalDivider(color = colors.divider)
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            timeSlots.forEachIndexed { index, slot ->
                AnimatedReveal(key = "${slot.timeRange}-$index") {
                    LessonTableRow(
                        slot = slot,
                        currentWeekType = currentWeekType,
                        weekFilter = weekFilter,
                        date = date,
                        isDateMode = isDateMode,
                        isCurrentSlot = slot.startTime == currentLesson?.startTime,
                        isNextSlot = slot.startTime == nextLesson?.startTime,
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

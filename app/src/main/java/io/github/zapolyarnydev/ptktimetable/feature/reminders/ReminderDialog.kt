package io.github.zapolyarnydev.ptktimetable.feature.reminders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zapolyarnydev.ptktimetable.core.ui.AppChoiceChip
import io.github.zapolyarnydev.ptktimetable.core.ui.AppModalScaffold
import io.github.zapolyarnydev.ptktimetable.feature.notes.ModalActions
import io.github.zapolyarnydev.ptktimetable.feature.notes.ScheduleNoteItem
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleLessonItem
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppIcons
import io.github.zapolyarnydev.ptktimetable.ui.theme.MaterialThemeAppColors
import java.time.LocalDate

@Composable
internal fun ReminderDialog(
    lesson: ScheduleLessonItem,
    date: LocalDate,
    note: ScheduleNoteItem?,
    canEdit: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (Boolean, Int) -> Unit,
) {
    var enabled by remember(note?.noteId) { mutableStateOf(note?.reminderEnabled == true) }
    var minutesText by remember(note?.noteId) { mutableStateOf((note?.reminderMinutes ?: 10).toString()) }
    val parsedMinutes = minutesText.toIntOrNull()?.coerceIn(1, 360)

    AppModalScaffold(
        title = if (note?.reminderEnabled == true) "Изменить напоминание" else "Новое напоминание",
        subtitle = "${io.github.zapolyarnydev.ptktimetable.core.ui.formatDateTitle(
            date,
        )} · ${lesson.timeRange} · ${lesson.subject.ifBlank {
            "Занятие"
        }}",
        onDismiss = onDismiss,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(AppIcons.reminder, contentDescription = null, tint = MaterialThemeAppColors.accent)
                Text("Включить уведомление", style = MaterialTheme.typography.bodyLarge)
            }
            Switch(checked = enabled, onCheckedChange = { enabled = it }, enabled = canEdit)
        }
        if (enabled) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = minutesText,
                onValueChange = { minutesText = it.filter(Char::isDigit).take(3) },
                label = { Text("Минут до начала") },
                modifier = Modifier.fillMaxWidth(),
                enabled = canEdit,
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf(5, 10, 15, 30, 60).forEach { minutes ->
                    AppChoiceChip(
                        selected = parsedMinutes == minutes,
                        label = "$minutes мин",
                        icon = AppIcons.time,
                        onClick = { minutesText = minutes.toString() },
                    )
                }
            }
        }
        if (!canEdit) {
            Spacer(Modifier.height(9.dp))
            Text(
                "Уведомления доступны только для будущих пар в режиме «По дате».",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialThemeAppColors.textSecondary,
            )
        }
        errorMessage?.let {
            Spacer(Modifier.height(9.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialThemeAppColors.error,
            )
        }
        Spacer(Modifier.height(16.dp))
        ModalActions(
            onDismiss = onDismiss,
            onSave = { onSave(enabled, parsedMinutes ?: 10) },
            saveEnabled = canEdit && (!enabled || parsedMinutes != null),
        )
    }
}

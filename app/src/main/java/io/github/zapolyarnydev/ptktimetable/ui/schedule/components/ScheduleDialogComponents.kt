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
internal fun LessonNoteDialog(
    lesson: ScheduleLessonItem,
    note: ScheduleNoteItem?,
    canEdit: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: () -> Unit
) {
    var noteText by remember(note?.noteId) { mutableStateOf(note?.noteText.orEmpty()) }
    AppModalDialog(
        title = "Заметка к занятию",
        subtitle = "",
        onDismiss = onDismiss
    ) {
        Text(
            text = "${lesson.day.title}, ${lesson.timeRange}",
            style = MaterialTheme.typography.bodySmall,
            color = InkSecondary
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Текст заметки") },
            enabled = canEdit,
            minLines = 4
        )
        if (!canEdit) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Редактирование доступно только для будущих пар в режиме «По дате».",
                style = MaterialTheme.typography.bodySmall,
                color = InkSecondary
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (note != null && canEdit) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Отмена", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.error)
                ) {
                    Text("Удалить", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            } else {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Отмена", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { onSave(noteText) },
            enabled = canEdit && noteText.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("Сохранить", maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
internal fun NotesOverviewDialog(
    notes: List<ScheduleNoteItem>,
    onDismiss: () -> Unit,
    onEdit: (String) -> Unit
) {
    AppModalDialog(
        title = "Все заметки",
        subtitle = "Нажмите на заметку, чтобы отредактировать её текст.",
        onDismiss = onDismiss
    ) {
        if (notes.isEmpty()) {
            Text(
                text = "Пока нет заметок",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSecondary
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp, max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notes, key = { it.noteId }) { note ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEdit(note.noteId) },
                        color = White,
                        border = BorderStroke(0.8.dp, BorderSubtle),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "${formatDateTitle(note.date)} • ${note.timeRange}",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSecondary
                            )
                            Text(
                                text = note.subject.ifBlank { "Пара" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = note.noteText.ifBlank { "Без текста заметки" },
                                style = MaterialTheme.typography.bodySmall,
                                color = NovsuBlue
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("Закрыть")
        }
    }
}

@Composable
internal fun ReminderDialog(
    lesson: ScheduleLessonItem,
    note: ScheduleNoteItem?,
    canEdit: Boolean,
    onDismiss: () -> Unit,
    onSave: (Boolean, Int) -> Unit
) {
    var enabled by remember(note?.noteId) { mutableStateOf(note?.reminderEnabled == true) }
    var minutesText by remember(note?.noteId) { mutableStateOf((note?.reminderMinutes ?: 10).toString()) }
    val quickOptions = listOf(5, 10, 15, 30, 60)
    val parsedMinutes = minutesText.toIntOrNull()?.coerceIn(1, 360)
    AppModalDialog(
        title = "Напоминание о паре",
        subtitle = "Настройте время уведомления. Если к паре есть заметка, она будет показана в тексте уведомления.",
        onDismiss = onDismiss
    ) {
        Text(
            text = "${lesson.day.title}, ${lesson.timeRange}",
            style = MaterialTheme.typography.bodySmall,
            color = InkSecondary
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Включить уведомление",
                style = MaterialTheme.typography.bodyMedium,
                color = InkPrimary
            )
            Switch(
                checked = enabled,
                onCheckedChange = { enabled = it },
                enabled = canEdit
            )
        }
        if (enabled) {
            OutlinedTextField(
                value = minutesText,
                onValueChange = { minutesText = it.filter(Char::isDigit).take(3) },
                label = { Text("Минут до начала") },
                modifier = Modifier.fillMaxWidth(),
                enabled = canEdit,
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(quickOptions, key = { it }) { item ->
                    WeekChip(
                        selected = parsedMinutes == item,
                        label = "$item мин",
                        icon = Icons.Outlined.Tune,
                        onClick = { minutesText = item.toString() }
                    )
                }
            }
        }
        if (!canEdit) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Уведомления доступны только для будущих пар в режиме «По дате».",
                style = MaterialTheme.typography.bodySmall,
                color = InkSecondary
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Отмена")
            }
            Button(
                onClick = { onSave(enabled, parsedMinutes ?: 10) },
                enabled = canEdit && (!enabled || parsedMinutes != null),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Сохранить")
            }
        }
    }
}

@Composable
internal fun NoteEditByIdDialog(
    note: ScheduleNoteItem,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: () -> Unit
) {
    var text by remember(note.noteId) { mutableStateOf(note.noteText) }
    AppModalDialog(
        title = "Редактирование заметки",
        subtitle = "${formatDateTitle(note.date)} • ${note.timeRange}",
        onDismiss = onDismiss
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Текст заметки") },
            minLines = 4
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Отмена", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.error)
            ) {
                Text("Удалить", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { onSave(text) },
            enabled = text.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("Сохранить", maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
internal fun AppModalDialog(
    title: String,
    subtitle: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(InkPrimary.copy(alpha = 0.28f))
                .padding(horizontal = 18.dp, vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 320.dp, max = 580.dp),
                color = SurfaceBlueTint,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BorderStrong)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    content = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = InkPrimary,
                            fontFamily = HeadingFontFamily
                        )
                        if (subtitle.isNotBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = InkMuted
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        content()
                    }
                )
            }
        }
    }
}


package io.github.zapolyarnydev.ptktimetable.feature.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleLessonItem
import io.github.zapolyarnydev.ptktimetable.ui.schedule.formatDateTitle
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppIcons
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppShapes

@Composable
internal fun LessonNoteDialog(
    lesson: ScheduleLessonItem,
    note: ScheduleNoteItem?,
    canEdit: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: () -> Unit,
) {
    var noteText by remember(note?.noteId) { mutableStateOf(note?.noteText.orEmpty()) }
    AppModalDialog("Заметка к занятию", "${lesson.day.title} · ${lesson.timeRange}", onDismiss) {
        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Текст заметки") },
            enabled = canEdit,
            minLines = 4,
        )
        if (!canEdit) {
            Spacer(Modifier.height(9.dp))
            Text(
                "Редактирование доступно только для будущих пар в режиме «По дате».",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(16.dp))
        ModalActions(
            onDismiss = onDismiss,
            onDelete = if (note != null && canEdit) onDelete else null,
            onSave = { onSave(noteText) },
            saveEnabled = canEdit && noteText.isNotBlank(),
        )
    }
}

@Composable
internal fun NotesOverviewDialog(notes: List<ScheduleNoteItem>, onDismiss: () -> Unit, onEdit: (String) -> Unit) {
    AppModalDialog("Все заметки", "Нажмите на заметку, чтобы открыть её", onDismiss) {
        if (notes.isEmpty()) {
            Text(
                "Пока нет заметок",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(min = 240.dp, max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(notes, key = { it.noteId }) { note ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppShapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = { onEdit(note.noteId) },
                    ) {
                        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "${formatDateTitle(note.date)} · ${note.timeRange}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                note.subject.ifBlank {
                                    "Пара"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                note.noteText.ifBlank {
                                    "Без текста заметки"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 3,
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = AppShapes.pill) { Text("Закрыть") }
    }
}

@Composable
internal fun NoteEditByIdDialog(
    note: ScheduleNoteItem,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: () -> Unit,
) {
    var text by remember(note.noteId) { mutableStateOf(note.noteText) }
    AppModalDialog("Редактирование заметки", "${formatDateTitle(note.date)} · ${note.timeRange}", onDismiss) {
        OutlinedTextField(value = text, onValueChange = {
            text = it
        }, modifier = Modifier.fillMaxWidth(), label = { Text("Текст заметки") }, minLines = 4)
        Spacer(Modifier.height(16.dp))
        ModalActions(onDismiss = onDismiss, onDelete = onDelete, onSave = {
            onSave(text)
        }, saveEnabled = text.isNotBlank())
    }
}

@Composable
internal fun ModalActions(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    saveEnabled: Boolean,
    onDelete: (() -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onDismiss) { Text("Отмена") }
            if (onDelete != null) {
                TextButton(
                    onClick = onDelete,
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Удалить")
                }
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = onSave, enabled = saveEnabled, shape = AppShapes.pill) { Text("Сохранить") }
        }
    }
}

@Composable
internal fun AppModalDialog(
    title: String,
    subtitle: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(min = 280.dp, max = 620.dp),
                color = MaterialTheme.colorScheme.surface,
                shape = AppShapes.large,
                tonalElevation = 6.dp,
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()).padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(title, style = MaterialTheme.typography.headlineSmall)
                            if (subtitle.isNotBlank()) {
                                Text(
                                    subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) { Icon(AppIcons.close, contentDescription = "Закрыть") }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    content()
                }
            }
        }
    }
}

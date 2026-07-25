package io.github.zapolyarnydev.ptktimetable.feature.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.zapolyarnydev.ptktimetable.data.local.LessonNote
import io.github.zapolyarnydev.ptktimetable.data.local.LessonNotesStore
import io.github.zapolyarnydev.ptktimetable.data.notification.LessonReminderWorkflow
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.ScheduleMode
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleLessonItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class NotesViewModel(
    private val notesStore: LessonNotesStore,
    private val reminderWorkflow: LessonReminderWorkflow,
    private val nowProvider: () -> Instant = { Instant.now() },
) : ViewModel() {

    private val _state = MutableStateFlow(NotesUiState())
    val state: StateFlow<NotesUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            notesStore.observeAll().collect { notes ->
                _state.update { current ->
                    current.withNotes(
                        notes.sortedWith(
                            compareBy<LessonNote> { it.date }
                                .thenBy { it.startTime }
                                .thenBy { it.createdAtEpochMillis },
                        ).map { it.toUiNote() },
                    )
                }
            }
        }
    }

    fun onAction(action: NotesUiAction) {
        when (action) {
            is NotesUiAction.UpdateScheduleContext -> _state.update {
                it.copy(
                    groupName = action.groupName,
                    selectedDate = action.selectedDate,
                    scheduleMode = action.mode,
                ).rebuildPresentation()
            }

            is NotesUiAction.OpenNote -> updateDialogs {
                NotesDialogState(noteLesson = action.lesson)
            }

            is NotesUiAction.OpenReminder -> updateDialogs {
                NotesDialogState(reminderLesson = action.lesson)
            }

            NotesUiAction.OpenOverview -> updateDialogs {
                NotesDialogState(showOverview = true)
            }

            is NotesUiAction.EditNote -> updateDialogs {
                NotesDialogState(editingNoteId = action.noteId)
            }

            NotesUiAction.DismissDialog -> updateDialogs { NotesDialogState() }

            is NotesUiAction.SaveLessonNote -> {
                state.value.dialogs.noteLesson?.let { saveNote(it, action.text) }
                dismissDialogs()
            }

            NotesUiAction.DeleteLessonNote -> {
                state.value.dialogs.noteLesson?.let(::deleteNote)
                dismissDialogs()
            }

            is NotesUiAction.SaveReminder -> {
                state.value.dialogs.reminderLesson?.let {
                    saveReminder(it, action.enabled, action.minutes)
                }
                dismissDialogs()
            }

            is NotesUiAction.UpdateNote -> {
                state.value.dialogs.editingNoteId?.let { updateNote(it, action.text) }
                dismissDialogs()
            }

            NotesUiAction.DeleteNote -> {
                state.value.dialogs.editingNoteId?.let(::deleteNote)
                dismissDialogs()
            }

            NotesUiAction.NotificationPermissionDenied -> _state.update {
                it.copy(errorMessage = "Без разрешения Android не покажет напоминание")
            }
        }
    }

    private fun saveNote(lesson: ScheduleLessonItem, text: String) {
        val context = editableContext(lesson) ?: return
        val trimmed = text.trim()
        if (trimmed.isBlank()) {
            showError("Введите текст заметки")
            return
        }
        launchMutation("Не удалось сохранить заметку") {
            val existing = findNote(context.groupName, context.date, lesson)
            val note = lesson.toNote(
                id = existing?.id ?: notesStore.newId(),
                groupName = context.groupName,
                date = context.date,
                noteText = trimmed,
                existing = existing,
            )
            notesStore.upsert(reminderWorkflow.reschedule(note))
        }
    }

    private fun saveReminder(lesson: ScheduleLessonItem, enabled: Boolean, minutes: Int) {
        val context = editableContext(lesson) ?: return
        launchMutation("Не удалось сохранить уведомление") {
            val existing = findNote(context.groupName, context.date, lesson)
            val note = lesson.toNote(
                id = existing?.id ?: notesStore.newId(),
                groupName = context.groupName,
                date = context.date,
                noteText = existing?.noteText.orEmpty(),
                existing = existing,
            )
            val updated = when {
                !enabled -> reminderWorkflow.cancel(note)
                existing?.reminderEnabled == true -> reminderWorkflow.change(note, minutes)
                else -> reminderWorkflow.create(note, minutes)
            }
            notesStore.upsert(updated)
        }
    }

    private fun deleteNote(lesson: ScheduleLessonItem) {
        val groupName = state.value.groupName ?: return
        val date = state.value.selectedDate
        launchMutation("Не удалось удалить заметку") {
            val existing = findNote(groupName, date, lesson) ?: return@launchMutation
            if (existing.reminderEnabled) {
                notesStore.upsert(reminderWorkflow.reschedule(existing.copy(noteText = "")))
            } else {
                notesStore.remove(existing.id)
            }
        }
    }

    private fun updateNote(noteId: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) {
            showError("Текст заметки не может быть пустым")
            return
        }
        launchMutation("Не удалось обновить заметку") {
            val existing = notesStore.getById(noteId) ?: return@launchMutation
            notesStore.upsert(reminderWorkflow.reschedule(existing.copy(noteText = trimmed)))
        }
    }

    private fun deleteNote(noteId: String) {
        launchMutation("Не удалось удалить заметку") {
            val existing = notesStore.getById(noteId)
            if (existing != null && existing.reminderEnabled) {
                notesStore.upsert(reminderWorkflow.reschedule(existing.copy(noteText = "")))
            } else {
                notesStore.remove(noteId)
            }
        }
    }

    private fun editableContext(lesson: ScheduleLessonItem): EditableContext? {
        val current = state.value
        val groupName = current.groupName ?: return null
        val editable = current.scheduleMode == ScheduleMode.BY_DATE &&
            !LocalDateTime.of(current.selectedDate, lesson.startTime)
                .isBefore(LocalDateTime.ofInstant(nowProvider(), ZoneId.systemDefault()))
        if (!editable) {
            showError("Редактирование доступно только для текущих и будущих пар")
            return null
        }
        return EditableContext(groupName, current.selectedDate)
    }

    private fun launchMutation(fallbackMessage: String, mutation: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { mutation() }
                .onSuccess { _state.update { it.copy(errorMessage = null) } }
                .onFailure { error -> showError(error.message ?: fallbackMessage) }
        }
    }

    private fun updateDialogs(transform: (NotesDialogState) -> NotesDialogState) {
        _state.update {
            it.copy(
                dialogs = transform(it.dialogs),
                errorMessage = null,
            ).rebuildPresentation()
        }
    }

    private fun dismissDialogs() {
        updateDialogs { NotesDialogState() }
    }

    private fun showError(message: String) {
        _state.update { it.copy(errorMessage = message) }
    }

    private suspend fun findNote(
        groupName: String,
        date: java.time.LocalDate,
        lesson: ScheduleLessonItem,
    ): LessonNote? = notesStore.findForLesson(
        groupName = groupName,
        date = date,
        startTime = lesson.startTime,
        endTime = lesson.endTime,
        weekType = lesson.weekType,
        rawText = lesson.rawText,
    )

    private fun NotesUiState.withNotes(notes: List<ScheduleNoteItem>): NotesUiState =
        copy(notes = notes).rebuildPresentation()

    private fun NotesUiState.rebuildPresentation(): NotesUiState {
        val groupNotes = notes.filter { groupName == null || it.groupName == groupName }
        val lesson = dialogs.noteLesson ?: dialogs.reminderLesson
        val now = LocalDateTime.ofInstant(nowProvider(), ZoneId.systemDefault())
        return copy(
            reminderMap = groupNotes.associateBy {
                noteLessonKey(it.date, it.timeRange, it.weekType, it.subject, it.rawText)
            },
            noteTextMap = groupNotes.filter { it.noteText.isNotBlank() }.associateBy {
                noteLessonKey(it.date, it.timeRange, it.weekType, it.subject, it.rawText)
            },
            canEditDialog = lesson != null &&
                scheduleMode == ScheduleMode.BY_DATE &&
                !LocalDateTime.of(selectedDate, lesson.startTime).isBefore(now),
        )
    }

    private fun LessonNote.toUiNote(): ScheduleNoteItem = ScheduleNoteItem(
        noteId = id,
        groupName = groupName,
        date = date,
        timeRange = timeRange,
        weekType = weekType,
        subject = subject,
        teacher = teacher,
        classroom = classroom,
        rawText = rawText,
        noteText = noteText,
        reminderEnabled = reminderEnabled,
        reminderMinutes = reminderMinutes,
        remindAtEpochMillis = remindAtEpochMillis,
        createdAtEpochMillis = createdAtEpochMillis,
    )

    private fun ScheduleLessonItem.toNote(
        id: String,
        groupName: String,
        date: java.time.LocalDate,
        noteText: String,
        existing: LessonNote?,
    ): LessonNote = LessonNote(
        id = id,
        groupName = groupName,
        date = date,
        startTime = startTime,
        endTime = endTime,
        weekType = weekType,
        subject = subject,
        teacher = teacher,
        classroom = classroom,
        rawText = rawText,
        noteText = noteText,
        reminderId = existing?.reminderId,
        reminderEnabled = existing?.reminderEnabled == true,
        reminderMinutes = existing?.reminderMinutes,
        remindAtEpochMillis = existing?.remindAtEpochMillis,
        createdAtEpochMillis = existing?.createdAtEpochMillis ?: nowProvider().toEpochMilli(),
    )

    private data class EditableContext(val groupName: String, val date: java.time.LocalDate)
}

class NotesViewModelFactory(
    private val notesStore: LessonNotesStore,
    private val reminderWorkflow: LessonReminderWorkflow,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotesViewModel(notesStore, reminderWorkflow) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

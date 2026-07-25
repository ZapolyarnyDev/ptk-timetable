package io.github.zapolyarnydev.ptktimetable.feature.notes

import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.ScheduleMode
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleLessonItem
import java.time.LocalDate

data class ScheduleNoteItem(
    val noteId: String,
    val groupName: String,
    val date: LocalDate,
    val timeRange: String,
    val weekType: WeekType,
    val subject: String,
    val teacher: String?,
    val classroom: String?,
    val rawText: String,
    val noteText: String,
    val reminderEnabled: Boolean,
    val reminderMinutes: Int?,
    val remindAtEpochMillis: Long?,
    val createdAtEpochMillis: Long,
)

data class NotesDialogState(
    val noteLesson: ScheduleLessonItem? = null,
    val reminderLesson: ScheduleLessonItem? = null,
    val editingNoteId: String? = null,
    val showOverview: Boolean = false,
)

data class NotesUiState(
    val groupName: String? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val scheduleMode: ScheduleMode = ScheduleMode.BY_DAY,
    val notes: List<ScheduleNoteItem> = emptyList(),
    val noteTextMap: Map<String, ScheduleNoteItem> = emptyMap(),
    val reminderMap: Map<String, ScheduleNoteItem> = emptyMap(),
    val dialogs: NotesDialogState = NotesDialogState(),
    val canEditDialog: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface NotesUiAction {
    data class UpdateScheduleContext(val groupName: String?, val selectedDate: LocalDate, val mode: ScheduleMode) :
        NotesUiAction

    data class OpenNote(val lesson: ScheduleLessonItem) : NotesUiAction

    data class OpenReminder(val lesson: ScheduleLessonItem) : NotesUiAction

    data object OpenOverview : NotesUiAction

    data class EditNote(val noteId: String) : NotesUiAction

    data object DismissDialog : NotesUiAction

    data class SaveLessonNote(val text: String) : NotesUiAction

    data object DeleteLessonNote : NotesUiAction

    data class SaveReminder(val enabled: Boolean, val minutes: Int) : NotesUiAction

    data class UpdateNote(val text: String) : NotesUiAction

    data object DeleteNote : NotesUiAction

    data object NotificationPermissionDenied : NotesUiAction
}

internal fun noteLessonKey(
    date: LocalDate,
    timeRange: String,
    weekType: WeekType,
    subject: String,
    rawText: String,
): String = listOf(
    date.toString(),
    timeRange.trim(),
    weekType.name,
    subject.trim(),
    rawText.trim().hashCode().toString(),
).joinToString("|")

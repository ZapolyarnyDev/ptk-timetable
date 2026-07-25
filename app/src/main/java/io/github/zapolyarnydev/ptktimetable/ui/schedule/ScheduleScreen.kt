package io.github.zapolyarnydev.ptktimetable.ui.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.ScheduleMode
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekFilter
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppAnimations
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppDimensions
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppIcons
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppShapes
import io.github.zapolyarnydev.ptktimetable.ui.theme.MaterialThemeAppColors
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

@Composable
fun ScheduleScreen(
    state: StateFlow<ScheduleUiState>,
    onRefresh: () -> Unit,
    onBackToGroups: () -> Unit,
    onSelectMode: (ScheduleMode) -> Unit,
    onSelectDay: (ScheduleDay) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onGoToToday: () -> Unit,
    onSelectWeekFilter: (WeekFilter) -> Unit,
    onSaveLessonNote: (ScheduleLessonItem, String) -> Unit,
    onSetLessonReminder: (ScheduleLessonItem, Boolean, Int) -> Unit,
    onDeleteLessonNote: (ScheduleLessonItem) -> Unit,
    onUpdateNoteById: (String, String) -> Unit,
    onDeleteNoteById: (String) -> Unit,
) {
    val uiState by state.collectAsStateWithLifecycle()
    ScheduleScreenContent(
        state = uiState,
        onRefresh = onRefresh,
        onBackToGroups = onBackToGroups,
        onSelectMode = onSelectMode,
        onSelectDay = onSelectDay,
        onPreviousDay = onPreviousDay,
        onNextDay = onNextDay,
        onSelectDate = onSelectDate,
        onPreviousDate = onPreviousDate,
        onNextDate = onNextDate,
        onGoToToday = onGoToToday,
        onSelectWeekFilter = onSelectWeekFilter,
        onSaveLessonNote = onSaveLessonNote,
        onSetLessonReminder = onSetLessonReminder,
        onDeleteLessonNote = onDeleteLessonNote,
        onUpdateNoteById = onUpdateNoteById,
        onDeleteNoteById = onDeleteNoteById,
    )
}

@Composable
private fun ScheduleScreenContent(
    state: ScheduleUiState,
    onRefresh: () -> Unit,
    onBackToGroups: () -> Unit,
    onSelectMode: (ScheduleMode) -> Unit,
    onSelectDay: (ScheduleDay) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onGoToToday: () -> Unit,
    onSelectWeekFilter: (WeekFilter) -> Unit,
    onSaveLessonNote: (ScheduleLessonItem, String) -> Unit,
    onSetLessonReminder: (ScheduleLessonItem, Boolean, Int) -> Unit,
    onDeleteLessonNote: (ScheduleLessonItem) -> Unit,
    onUpdateNoteById: (String, String) -> Unit,
    onDeleteNoteById: (String) -> Unit,
) {
    val colors = MaterialThemeAppColors
    Scaffold(containerColor = colors.canvas) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            ScheduleState(
                state = state,
                onRefresh = onRefresh,
                onBackToGroups = onBackToGroups,
                onSelectMode = onSelectMode,
                onSelectDay = onSelectDay,
                onPreviousDay = onPreviousDay,
                onNextDay = onNextDay,
                onSelectDate = onSelectDate,
                onPreviousDate = onPreviousDate,
                onNextDate = onNextDate,
                onGoToToday = onGoToToday,
                onSelectWeekFilter = onSelectWeekFilter,
                onSaveLessonNote = onSaveLessonNote,
                onSetLessonReminder = onSetLessonReminder,
                onDeleteLessonNote = onDeleteLessonNote,
                onUpdateNoteById = onUpdateNoteById,
                onDeleteNoteById = onDeleteNoteById,
            )

            if (state.isRefreshing) RefreshingIndicator()
            if (state.isInitialLoading) InitialLoadingState()
        }
    }
}

@Composable
private fun InitialLoadingState() {
    val colors = MaterialThemeAppColors
    Box(Modifier.fillMaxSize().background(colors.canvas), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Box(
                modifier = Modifier.size(76.dp).clip(AppShapes.large).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    AppIcons.schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(38.dp),
                )
            }
            Text("Расписание", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Подготавливаем данные…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CircularProgressIndicator(modifier = Modifier.size(26.dp), strokeWidth = 2.5.dp)
        }
    }
}

@Composable
private fun RefreshingIndicator() {
    Box(
        Modifier.fillMaxSize().padding(top = AppDimensions.screenVerticalPadding),
        contentAlignment = Alignment.TopCenter,
    ) {
        Surface(shape = AppShapes.large, color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
            Row(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 17.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.5.dp)
                Text("Обновляем расписание…", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun ScheduleState(
    state: ScheduleUiState,
    onRefresh: () -> Unit,
    onBackToGroups: () -> Unit,
    onSelectMode: (ScheduleMode) -> Unit,
    onSelectDay: (ScheduleDay) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onGoToToday: () -> Unit,
    onSelectWeekFilter: (WeekFilter) -> Unit,
    onSaveLessonNote: (ScheduleLessonItem, String) -> Unit,
    onSetLessonReminder: (ScheduleLessonItem, Boolean, Int) -> Unit,
    onDeleteLessonNote: (ScheduleLessonItem) -> Unit,
    onUpdateNoteById: (String, String) -> Unit,
    onDeleteNoteById: (String) -> Unit,
) {
    var editingLesson by remember { mutableStateOf<ScheduleLessonItem?>(null) }
    var reminderLesson by remember { mutableStateOf<ScheduleLessonItem?>(null) }
    var editingNoteId by remember { mutableStateOf<String?>(null) }
    var showNotesDialog by remember { mutableStateOf(false) }

    val filteredLessons = ScheduleRules.visibleLessons(state)
    val timeSlots = buildTimeSlots(filteredLessons)
    val activeGroup = state.selectedGroup?.groupName
    val notesForGroup = state.notes.filter { activeGroup.isNullOrBlank() || it.groupName == activeGroup }
    val lessonEntryMap = notesForGroup.associateBy {
        noteLessonKey(it.date, it.timeRange, it.weekType, it.subject, it.rawText)
    }
    val noteTextMap = notesForGroup.filter { it.noteText.isNotBlank() }.associateBy {
        noteLessonKey(it.date, it.timeRange, it.weekType, it.subject, it.rawText)
    }
    val dayIndex = state.availableDays.indexOf(state.selectedDay).takeIf { it >= 0 } ?: 0
    val canGoPrev = if (state.mode == ScheduleMode.BY_DAY) dayIndex > 0 else true
    val canGoNext = if (state.mode == ScheduleMode.BY_DAY) dayIndex < state.availableDays.lastIndex else true
    val currentLesson = ScheduleRules.currentLesson(
        lessons = filteredLessons,
        date = state.selectedDate,
        selectedDay = state.selectedDay,
        isDateMode = state.mode == ScheduleMode.BY_DATE,
    )
    val nextLesson = ScheduleRules.nextLesson(
        lessons = filteredLessons,
        date = state.selectedDate,
        selectedDay = state.selectedDay,
        isDateMode = state.mode == ScheduleMode.BY_DATE,
    )

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = AppDimensions.screenVerticalPadding),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.sectionSpacing),
        ) {
            item {
                Box(Modifier.padding(horizontal = AppDimensions.screenHorizontalPadding)) {
                    DayNavigatorPanel(
                        mode = state.mode,
                        selectedDayTitle = state.selectedDay?.title ?: "День не выбран",
                        selectedDate = state.selectedDate,
                        currentWeekType = if (state.mode ==
                            ScheduleMode.BY_DATE
                        ) {
                            state.selectedDateWeekType
                        } else {
                            state.currentWeekType
                        },
                        dayIndex = dayIndex,
                        totalDays = state.availableDays.size,
                        canGoPrev = canGoPrev,
                        canGoNext = canGoNext,
                        onSelectMode = onSelectMode,
                        onPreviousDay = onPreviousDay,
                        onNextDay = onNextDay,
                        onSelectDate = onSelectDate,
                        onPreviousDate = onPreviousDate,
                        onNextDate = onNextDate,
                        onGoToToday = onGoToToday,
                        availableDays = state.availableDays,
                        selectedDay = state.selectedDay,
                        weekFilter = state.weekFilter,
                        onSelectDay = onSelectDay,
                        onSelectWeekFilter = onSelectWeekFilter,
                        groupTitle = state.selectedGroup?.let { "Группа ${it.groupName}" },
                        courseTitle = state.selectedGroup?.courseName,
                        onBack = onBackToGroups,
                        onRefresh = onRefresh,
                        errorMessage = syncMessage(state),
                    )
                }
            }
            if (currentLesson != null || nextLesson != null) {
                item {
                    Box(Modifier.padding(horizontal = AppDimensions.screenHorizontalPadding)) {
                        LessonStatusSummary(currentLesson, nextLesson)
                    }
                }
            }
            item {
                Box(Modifier.padding(horizontal = AppDimensions.screenHorizontalPadding)) {
                    when {
                        state.isInitialLoading && state.lessons.isEmpty() -> LessonTableSkeleton()

                        timeSlots.isEmpty() -> EmptyStateBlock(
                            if (state.lessons.isEmpty()) "Занятий не найдено" else "На выбранный день и неделю пар нет",
                        )

                        else -> LessonTableCard(
                            timeSlots = timeSlots,
                            currentWeekType = if (state.mode ==
                                ScheduleMode.BY_DATE
                            ) {
                                state.selectedDateWeekType
                            } else {
                                state.currentWeekType
                            },
                            weekFilter = state.weekFilter,
                            date = state.selectedDate,
                            selectedDay = state.selectedDay,
                            isDateMode = state.mode == ScheduleMode.BY_DATE,
                            noteMap = noteTextMap,
                            reminderMap = lessonEntryMap,
                            onAddOrEditNote = { editingLesson = it },
                            onAddOrEditReminder = { reminderLesson = it },
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showNotesDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            shape = AppShapes.medium,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) { Icon(AppIcons.notes, contentDescription = "Все заметки") }
    }

    if (showNotesDialog) {
        NotesOverviewDialog(
            notes = state.notes.filter { it.noteText.isNotBlank() },
            onDismiss = { showNotesDialog = false },
            onEdit = { noteId ->
                editingNoteId = noteId
                showNotesDialog = false
            },
        )
    }
    editingLesson?.let { lesson ->
        val note = noteTextMap[
            noteLessonKey(
                state.selectedDate,
                lesson.timeRange,
                lesson.weekType,
                lesson.subject,
                lesson.rawText,
            ),
        ]
        LessonNoteDialog(
            lesson = lesson,
            note = note,
            canEdit =
            state.mode == ScheduleMode.BY_DATE && ScheduleRules.isEditable(state.selectedDate, lesson),
            onDismiss = { editingLesson = null },
            onSave = { text ->
                onSaveLessonNote(lesson, text)
                editingLesson = null
            },
            onDelete = {
                onDeleteLessonNote(lesson)
                editingLesson = null
            },
        )
    }
    reminderLesson?.let { lesson ->
        val note = lessonEntryMap[
            noteLessonKey(
                state.selectedDate,
                lesson.timeRange,
                lesson.weekType,
                lesson.subject,
                lesson.rawText,
            ),
        ]
        ReminderDialog(
            lesson = lesson,
            note = note,
            canEdit =
            state.mode == ScheduleMode.BY_DATE && ScheduleRules.isEditable(state.selectedDate, lesson),
            onDismiss = { reminderLesson = null },
            onSave = { enabled, minutes ->
                onSetLessonReminder(lesson, enabled, minutes)
                reminderLesson = null
            },
        )
    }
    editingNoteId?.let { noteId ->
        state.notes.firstOrNull { it.noteId == noteId }?.let { note ->
            NoteEditByIdDialog(
                note = note,
                onDismiss = { editingNoteId = null },
                onSave = { text ->
                    onUpdateNoteById(noteId, text)
                    editingNoteId = null
                },
                onDelete = {
                    onDeleteNoteById(noteId)
                    editingNoteId = null
                },
            )
        }
    }
}

@Composable
private fun LessonStatusSummary(current: ScheduleLessonItem?, next: ScheduleLessonItem?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatusCard(
            modifier = Modifier.weight(1f),
            label = "Сейчас",
            lesson = current,
            accent = MaterialTheme.colorScheme.primary,
            container = MaterialTheme.colorScheme.surfaceVariant,
        )
        StatusCard(
            modifier = Modifier.weight(1f),
            label = "Дальше",
            lesson = next,
            accent = MaterialTheme.colorScheme.primary,
            container = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
private fun StatusCard(
    modifier: Modifier,
    label: String,
    lesson: ScheduleLessonItem?,
    accent: androidx.compose.ui.graphics.Color,
    container: androidx.compose.ui.graphics.Color,
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.medium,
        color = container,
        border = BorderStroke(
            width = 1.dp,
            color = if (lesson != null) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.42f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                fontWeight = FontWeight.Bold,
            )
            if (lesson == null) {
                Text(
                    "Нет занятия",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    lesson.timeRange,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    lesson.subject.ifBlank {
                        lesson.rawText
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                )
            }
        }
    }
}

private fun syncMessage(state: ScheduleUiState): String? {
    state.errorMessage?.let { return it }
    val syncError = state.syncError ?: return null
    return if (state.isOffline) {
        "Офлайн. Показаны сохранённые данные. $syncError"
    } else {
        syncError
    }
}

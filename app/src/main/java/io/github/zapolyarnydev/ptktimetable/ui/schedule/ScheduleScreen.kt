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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zapolyarnydev.ptktimetable.core.ui.EmptyStateBlock
import io.github.zapolyarnydev.ptktimetable.core.ui.FullScreenErrorState
import io.github.zapolyarnydev.ptktimetable.core.ui.LessonTableSkeleton
import io.github.zapolyarnydev.ptktimetable.core.ui.SectionCard
import io.github.zapolyarnydev.ptktimetable.core.ui.SyncFeedback
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.ScheduleMode
import io.github.zapolyarnydev.ptktimetable.feature.notes.LessonNoteDialog
import io.github.zapolyarnydev.ptktimetable.feature.notes.NoteEditByIdDialog
import io.github.zapolyarnydev.ptktimetable.feature.notes.NotesOverviewDialog
import io.github.zapolyarnydev.ptktimetable.feature.notes.NotesUiAction
import io.github.zapolyarnydev.ptktimetable.feature.notes.NotesUiState
import io.github.zapolyarnydev.ptktimetable.feature.notes.NotesViewModel
import io.github.zapolyarnydev.ptktimetable.feature.notes.noteLessonKey
import io.github.zapolyarnydev.ptktimetable.feature.reminders.ReminderDialog
import io.github.zapolyarnydev.ptktimetable.feature.reminders.rememberPermissionAwareNotesAction
import io.github.zapolyarnydev.ptktimetable.feature.schedule.ui.DayNavigatorPanel
import io.github.zapolyarnydev.ptktimetable.feature.schedule.ui.LessonTableCard
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppAnimations
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppDimensions
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppIcons
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppShapes
import io.github.zapolyarnydev.ptktimetable.ui.theme.MaterialThemeAppColors

@Composable
fun ScheduleRoute(viewModel: ScheduleViewModel, notesViewModel: NotesViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val notesState by notesViewModel.state.collectAsStateWithLifecycle()
    val onNotesAction = rememberPermissionAwareNotesAction(notesViewModel::onAction)
    LaunchedEffect(viewModel, onBack) {
        viewModel.events.collect { event ->
            if (event == ScheduleUiEvent.NavigateBack) onBack()
        }
    }
    LaunchedEffect(state.selectedGroup?.groupName, state.selectedDate, state.mode) {
        notesViewModel.onAction(
            NotesUiAction.UpdateScheduleContext(
                groupName = state.selectedGroup?.groupName,
                selectedDate = state.selectedDate,
                mode = state.mode,
            ),
        )
    }
    ScheduleScreen(
        state = state,
        notesState = notesState,
        onAction = viewModel::onAction,
        onNotesAction = onNotesAction,
    )
}

@Composable
fun ScheduleScreen(
    state: ScheduleUiState,
    notesState: NotesUiState,
    onAction: (ScheduleUiAction) -> Unit,
    onNotesAction: (NotesUiAction) -> Unit,
) {
    val colors = MaterialThemeAppColors
    Scaffold(containerColor = colors.canvas) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isInitialLoading && state.lessons.isEmpty() -> InitialLoadingState()

                state.syncError != null && state.lessons.isEmpty() -> FullScreenErrorState(
                    title = "Расписание недоступно",
                    message = state.syncError,
                    onRetry = { onAction(ScheduleUiAction.Refresh) },
                    secondaryAction = "К группам" to { onAction(ScheduleUiAction.Back) },
                )

                else -> ScheduleState(
                    state = state,
                    notesState = notesState,
                    onAction = onAction,
                    onNotesAction = onNotesAction,
                )
            }
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
private fun ScheduleState(
    state: ScheduleUiState,
    notesState: NotesUiState,
    onAction: (ScheduleUiAction) -> Unit,
    onNotesAction: (NotesUiAction) -> Unit,
) {
    val data = state.data
    val navigation = state.dateNavigation
    val presentation = data.presentation

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = AppDimensions.screenVerticalPadding),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.sectionSpacing),
        ) {
            item {
                Box(Modifier.padding(horizontal = AppDimensions.screenHorizontalPadding)) {
                    DayNavigatorPanel(
                        mode = navigation.mode,
                        selectedDayTitle = navigation.selectedDay?.title ?: "День не выбран",
                        selectedDate = navigation.selectedDate,
                        currentWeekType = if (navigation.mode ==
                            ScheduleMode.BY_DATE
                        ) {
                            navigation.selectedDateWeekType
                        } else {
                            navigation.currentWeekType
                        },
                        weekMismatch = navigation.weekMismatch,
                        dayIndex = navigation.dayIndex,
                        totalDays = navigation.totalDays,
                        canGoPrev = navigation.canGoPrevious,
                        canGoNext = navigation.canGoNext,
                        onSelectMode = { onAction(ScheduleUiAction.SelectMode(it)) },
                        onPreviousDay = { onAction(ScheduleUiAction.PreviousDay) },
                        onNextDay = { onAction(ScheduleUiAction.NextDay) },
                        onSelectDate = { onAction(ScheduleUiAction.SelectDate(it)) },
                        onPreviousDate = { onAction(ScheduleUiAction.PreviousDate) },
                        onNextDate = { onAction(ScheduleUiAction.NextDate) },
                        onGoToToday = { onAction(ScheduleUiAction.Today) },
                        availableDays = data.availableDays,
                        selectedDay = navigation.selectedDay,
                        weekFilter = navigation.weekFilter,
                        onSelectDay = { onAction(ScheduleUiAction.SelectDay(it)) },
                        onSelectWeekFilter = { onAction(ScheduleUiAction.SelectWeekFilter(it)) },
                        groupTitle = data.selectedGroup?.let { "Группа ${it.groupName}" },
                        courseTitle = data.selectedGroup?.courseName,
                        onBack = { onAction(ScheduleUiAction.Back) },
                        onRefresh = { onAction(ScheduleUiAction.Refresh) },
                        errorMessage = notesState.errorMessage ?: state.errorMessage,
                    )
                }
            }
            if (data.updatedAt != null || data.isRefreshing || data.syncError != null) {
                item {
                    Box(Modifier.padding(horizontal = AppDimensions.screenHorizontalPadding)) {
                        SectionCard {
                            SyncFeedback(
                                updatedAt = data.updatedAt,
                                isRefreshing = data.isRefreshing,
                                syncError = data.syncError,
                                isOffline = data.isOffline,
                            )
                        }
                    }
                }
            }
            if (presentation.currentLesson != null || presentation.nextLesson != null) {
                item {
                    Box(Modifier.padding(horizontal = AppDimensions.screenHorizontalPadding)) {
                        LessonStatusSummary(presentation.currentLesson, presentation.nextLesson)
                    }
                }
            }
            item {
                Box(Modifier.padding(horizontal = AppDimensions.screenHorizontalPadding)) {
                    when {
                        data.isInitialLoading && data.lessons.isEmpty() -> LessonTableSkeleton()

                        presentation.timeSlots.isEmpty() -> EmptyStateBlock(
                            if (data.lessons.isEmpty()) "Занятий не найдено" else "На выбранный день и неделю пар нет",
                        )

                        else -> LessonTableCard(
                            timeSlots = presentation.timeSlots,
                            currentWeekType = if (navigation.mode ==
                                ScheduleMode.BY_DATE
                            ) {
                                navigation.selectedDateWeekType
                            } else {
                                navigation.currentWeekType
                            },
                            weekFilter = navigation.weekFilter,
                            date = navigation.selectedDate,
                            isDateMode = navigation.mode == ScheduleMode.BY_DATE,
                            currentLesson = presentation.currentLesson,
                            nextLesson = presentation.nextLesson,
                            noteMap = notesState.noteTextMap,
                            reminderMap = notesState.reminderMap,
                            onAddOrEditNote = { onNotesAction(NotesUiAction.OpenNote(it)) },
                            onAddOrEditReminder = { onNotesAction(NotesUiAction.OpenReminder(it)) },
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { onNotesAction(NotesUiAction.OpenOverview) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            shape = AppShapes.medium,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) { Icon(AppIcons.notes, contentDescription = "Все заметки") }
    }

    if (notesState.dialogs.showOverview) {
        NotesOverviewDialog(
            notes = notesState.notes.filter { it.noteText.isNotBlank() },
            onDismiss = { onNotesAction(NotesUiAction.DismissDialog) },
            onEdit = { onNotesAction(NotesUiAction.EditNote(it)) },
        )
    }
    notesState.dialogs.noteLesson?.let { lesson ->
        val note = notesState.noteTextMap[
            noteLessonKey(
                navigation.selectedDate,
                lesson.timeRange,
                lesson.weekType,
                lesson.subject,
                lesson.rawText,
            ),
        ]
        LessonNoteDialog(
            lesson = lesson,
            note = note,
            canEdit = notesState.canEditDialog,
            onDismiss = { onNotesAction(NotesUiAction.DismissDialog) },
            onSave = { onNotesAction(NotesUiAction.SaveLessonNote(it)) },
            onDelete = { onNotesAction(NotesUiAction.DeleteLessonNote) },
        )
    }
    notesState.dialogs.reminderLesson?.let { lesson ->
        val note = notesState.reminderMap[
            noteLessonKey(
                navigation.selectedDate,
                lesson.timeRange,
                lesson.weekType,
                lesson.subject,
                lesson.rawText,
            ),
        ]
        ReminderDialog(
            lesson = lesson,
            note = note,
            canEdit = notesState.canEditDialog,
            errorMessage = notesState.errorMessage,
            onDismiss = { onNotesAction(NotesUiAction.DismissDialog) },
            onSave = { enabled, minutes -> onNotesAction(NotesUiAction.SaveReminder(enabled, minutes)) },
        )
    }
    notesState.dialogs.editingNoteId?.let { noteId ->
        notesState.notes.firstOrNull { it.noteId == noteId }?.let { note ->
            NoteEditByIdDialog(
                note = note,
                onDismiss = { onNotesAction(NotesUiAction.DismissDialog) },
                onSave = { onNotesAction(NotesUiAction.UpdateNote(it)) },
                onDelete = { onNotesAction(NotesUiAction.DeleteNote) },
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

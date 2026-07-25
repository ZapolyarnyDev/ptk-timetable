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
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.ScheduleMode
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppAnimations
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppDimensions
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppIcons
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppShapes
import io.github.zapolyarnydev.ptktimetable.ui.theme.MaterialThemeAppColors

@Composable
fun ScheduleRoute(viewModel: ScheduleViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel, onBack) {
        viewModel.events.collect { event ->
            if (event == ScheduleUiEvent.NavigateBack) onBack()
        }
    }
    ScheduleScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun ScheduleScreen(state: ScheduleUiState, onAction: (ScheduleUiAction) -> Unit) {
    val colors = MaterialThemeAppColors
    Scaffold(containerColor = colors.canvas) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            ScheduleState(
                state = state,
                onAction = onAction,
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
private fun ScheduleState(state: ScheduleUiState, onAction: (ScheduleUiAction) -> Unit) {
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
                        errorMessage = syncMessage(state),
                    )
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
                            noteMap = presentation.noteTextMap,
                            reminderMap = presentation.reminderMap,
                            onAddOrEditNote = { onAction(ScheduleUiAction.OpenNote(it)) },
                            onAddOrEditReminder = { onAction(ScheduleUiAction.OpenReminder(it)) },
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { onAction(ScheduleUiAction.OpenNotesOverview) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            shape = AppShapes.medium,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) { Icon(AppIcons.notes, contentDescription = "Все заметки") }
    }

    if (state.dialogs.showNotesOverview) {
        NotesOverviewDialog(
            notes = data.notes.filter { it.noteText.isNotBlank() },
            onDismiss = { onAction(ScheduleUiAction.DismissDialog) },
            onEdit = { onAction(ScheduleUiAction.EditNote(it)) },
        )
    }
    state.dialogs.noteLesson?.let { lesson ->
        val note = presentation.noteTextMap[
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
            canEdit = presentation.canEditDialog,
            onDismiss = { onAction(ScheduleUiAction.DismissDialog) },
            onSave = { onAction(ScheduleUiAction.SaveLessonNote(it)) },
            onDelete = { onAction(ScheduleUiAction.DeleteLessonNote) },
        )
    }
    state.dialogs.reminderLesson?.let { lesson ->
        val note = presentation.reminderMap[
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
            canEdit = presentation.canEditDialog,
            onDismiss = { onAction(ScheduleUiAction.DismissDialog) },
            onSave = { enabled, minutes -> onAction(ScheduleUiAction.SaveReminder(enabled, minutes)) },
        )
    }
    state.dialogs.editingNoteId?.let { noteId ->
        data.notes.firstOrNull { it.noteId == noteId }?.let { note ->
            NoteEditByIdDialog(
                note = note,
                onDismiss = { onAction(ScheduleUiAction.DismissDialog) },
                onSave = { onAction(ScheduleUiAction.UpdateNote(it)) },
                onDelete = { onAction(ScheduleUiAction.DeleteNote) },
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

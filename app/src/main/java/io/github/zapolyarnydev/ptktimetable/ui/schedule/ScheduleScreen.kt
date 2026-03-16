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
import io.github.zapolyarnydev.ptktimetable.ui.theme.HeadingFontFamily
import io.github.zapolyarnydev.ptktimetable.ui.theme.InkPrimary
import io.github.zapolyarnydev.ptktimetable.ui.theme.InkSecondary
import io.github.zapolyarnydev.ptktimetable.ui.theme.MainFontFamily
import io.github.zapolyarnydev.ptktimetable.ui.theme.NovsuBlue
import io.github.zapolyarnydev.ptktimetable.ui.theme.NovsuBlueSoft
import io.github.zapolyarnydev.ptktimetable.ui.theme.SurfaceMuted
import io.github.zapolyarnydev.ptktimetable.ui.theme.White
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ScheduleScreen(
    state: StateFlow<ScheduleUiState>,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onCourseSelect: (CourseItem) -> Unit,
    onGroupSelect: (PtkGroupInfo) -> Unit,
    onBackToCourses: () -> Unit,
    onBackToGroups: () -> Unit,
    onSelectMode: (ScheduleMode) -> Unit,
    onSelectDay: (ScheduleDay) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onGoToToday: () -> Unit,
    onSelectWeekFilter: (ScheduleWeekFilter) -> Unit,
    onSaveLessonNote: (ScheduleLessonItem, String) -> Unit,
    onSetLessonReminder: (ScheduleLessonItem, Boolean, Int) -> Unit,
    onDeleteLessonNote: (ScheduleLessonItem) -> Unit,
    onUpdateNoteById: (String, String) -> Unit,
    onDeleteNoteById: (String) -> Unit
) {
    val uiState by state.collectAsStateWithLifecycle()
    ScheduleScreenContent(
        state = uiState,
        onRetry = onRetry,
        onRefresh = onRefresh,
        onCourseSelect = onCourseSelect,
        onGroupSelect = onGroupSelect,
        onBackToCourses = onBackToCourses,
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
        onDeleteNoteById = onDeleteNoteById
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleScreenContent(
    state: ScheduleUiState,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onCourseSelect: (CourseItem) -> Unit,
    onGroupSelect: (PtkGroupInfo) -> Unit,
    onBackToCourses: () -> Unit,
    onBackToGroups: () -> Unit,
    onSelectMode: (ScheduleMode) -> Unit,
    onSelectDay: (ScheduleDay) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onGoToToday: () -> Unit,
    onSelectWeekFilter: (ScheduleWeekFilter) -> Unit,
    onSaveLessonNote: (ScheduleLessonItem, String) -> Unit,
    onSetLessonReminder: (ScheduleLessonItem, Boolean, Int) -> Unit,
    onDeleteLessonNote: (ScheduleLessonItem) -> Unit,
    onUpdateNoteById: (String, String) -> Unit,
    onDeleteNoteById: (String) -> Unit
) {
    Scaffold(containerColor = White) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (state.step) {
                ScheduleStep.COURSE_SELECTION -> CourseSelectionState(
                    padding = padding,
                    state = state,
                    onRefresh = onRefresh,
                    onRetry = onRetry,
                    onCourseSelect = onCourseSelect
                )

                ScheduleStep.GROUP_SELECTION -> GroupSelectionState(
                    padding = padding,
                    state = state,
                    onRefresh = onRefresh,
                    onBackToCourses = onBackToCourses,
                    onGroupSelect = onGroupSelect
                )

                ScheduleStep.SCHEDULE -> ScheduleState(
                    padding = padding,
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
                    onDeleteNoteById = onDeleteNoteById
                )
            }

            if (state.isLoading) {
                LoadingOverlay()
            }
        }
    }
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(InkPrimary.copy(alpha = 0.22f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = White,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(0.8.dp, BorderSubtle),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = NovsuBlue,
                    strokeWidth = 2.4.dp
                )
                Text(
                    text = "Загружаем данные...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkPrimary
                )
            }
        }
    }
}

@Composable
private fun CourseSelectionState(
    padding: PaddingValues,
    state: ScheduleUiState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onCourseSelect: (CourseItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(White),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HeaderPanel(
                title = "Добро пожаловать",
                subtitle = "Выберите курс, затем группу и получите расписание",
                icon = Icons.Outlined.School
            )
        }

        item {
            InfoPanel {
                state.groupsUpdatedAt?.let {
                    MetaRow(
                        icon = Icons.Outlined.Update,
                        text = "Обновлено: ${formatInstant(it)}",
                        highlight = false
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    PrimaryActionButton(text = "Обновить", icon = Icons.Outlined.Refresh, onClick = onRefresh)
                    if (state.isLoading) InlineLoading()
                }
                state.errorMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    OutlinedActionButton(text = "Повторить", onClick = onRetry)
                }
            }
        }

        item {
            if (state.isLoading && state.courses.isEmpty()) {
                SelectionListSkeleton(rows = 4)
            } else if (state.courses.isEmpty()) {
                EmptyStateBlock(text = "Курсы не найдены")
            } else {
                SelectionListSection(
                    title = "Курсы",
                    items = state.courses,
                    icon = { Icons.Outlined.School },
                    titleText = { it.title },
                    subtitleText = { "Курс №${it.course}" },
                    onClick = onCourseSelect
                )
            }
        }
    }
}

@Composable
private fun GroupSelectionState(
    padding: PaddingValues,
    state: ScheduleUiState,
    onRefresh: () -> Unit,
    onBackToCourses: () -> Unit,
    onGroupSelect: (PtkGroupInfo) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(White),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            InfoPanel {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedActionButton(text = "К курсам", onClick = onBackToCourses)
                    PrimaryActionButton(text = "Обновить", icon = Icons.Outlined.Refresh, onClick = onRefresh)
                    if (state.isLoading) InlineLoading()
                }
                Spacer(Modifier.height(10.dp))
                MetaRow(icon = Icons.Outlined.School, text = "Курс: ${state.selectedCourse?.title ?: "-"}")
                state.errorMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        item {
            if (state.isLoading && state.courseGroups.isEmpty()) {
                SelectionListSkeleton(rows = 6)
            } else if (state.courseGroups.isEmpty()) {
                EmptyStateBlock(text = "Для выбранного курса группы не найдены")
            } else {
                SelectionListSection(
                    title = "Группы",
                    items = state.courseGroups,
                    icon = { Icons.Outlined.Groups },
                    titleText = { "Группа ${it.groupName}" },
                    subtitleText = { it.collegeName },
                    onClick = onGroupSelect
                )
            }
        }
    }
}

@Composable
private fun ScheduleState(
    padding: PaddingValues,
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
    onSelectWeekFilter: (ScheduleWeekFilter) -> Unit,
    onSaveLessonNote: (ScheduleLessonItem, String) -> Unit,
    onSetLessonReminder: (ScheduleLessonItem, Boolean, Int) -> Unit,
    onDeleteLessonNote: (ScheduleLessonItem) -> Unit,
    onUpdateNoteById: (String, String) -> Unit,
    onDeleteNoteById: (String) -> Unit
) {
    var editingLesson by remember { mutableStateOf<ScheduleLessonItem?>(null) }
    var reminderLesson by remember { mutableStateOf<ScheduleLessonItem?>(null) }
    var editingNoteId by remember { mutableStateOf<String?>(null) }
    var showNotesDialog by remember { mutableStateOf(false) }

    val filteredLessons = filterLessons(state)
    val timeSlots = buildTimeSlots(filteredLessons)
    val activeGroup = state.selectedGroup?.groupName
    val notesForGroup = state.notes.filter { note ->
        activeGroup.isNullOrBlank() || note.groupName == activeGroup
    }
    val lessonEntryMap = notesForGroup.associateBy { note ->
        noteLessonKey(
            date = note.date,
            timeRange = note.timeRange,
            weekType = note.weekType,
            subject = note.subject,
            rawText = note.rawText
        )
    }
    val noteTextMap = notesForGroup
        .filter { it.noteText.isNotBlank() }
        .associateBy { note ->
            noteLessonKey(
                date = note.date,
                timeRange = note.timeRange,
                weekType = note.weekType,
                subject = note.subject,
                rawText = note.rawText
            )
        }
    val dayIndex = state.availableDays.indexOf(state.selectedDay).takeIf { it >= 0 } ?: 0
    val canGoPrev = if (state.mode == ScheduleMode.BY_DAY) dayIndex > 0 else true
    val canGoNext = if (state.mode == ScheduleMode.BY_DAY) dayIndex < state.availableDays.lastIndex else true

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(White),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                InfoPanel {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedActionButton(text = "К группам", onClick = onBackToGroups)
                        PrimaryActionButton(text = "Обновить", icon = Icons.Outlined.Refresh, onClick = onRefresh)
                        if (state.isLoading) InlineLoading()
                    }
                    Spacer(Modifier.height(10.dp))
                    MetaRow(
                        icon = Icons.Outlined.Groups,
                        text = state.selectedGroup?.let { "Группа ${it.groupName}" } ?: "Группа не выбрана"
                    )
                    Spacer(Modifier.height(6.dp))
                    val activeWeekType = if (state.mode == ScheduleMode.BY_DATE) {
                        state.selectedDateWeekType
                    } else {
                        state.currentWeekType
                    }
                    MetaRow(
                        icon = Icons.Outlined.CalendarMonth,
                        text = if (state.mode == ScheduleMode.BY_DATE) {
                            "Неделя на дату: ${weekTypeLabel(activeWeekType)}"
                        } else {
                            "Текущая неделя: ${weekTypeLabel(activeWeekType)}"
                        }
                    )
                    state.scheduleUpdatedAt?.let {
                        Spacer(Modifier.height(6.dp))
                        MetaRow(
                            icon = Icons.Outlined.Update,
                            text = "Обновлено: ${formatInstant(it)}",
                            highlight = false
                        )
                    }
                    state.errorMessage?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                DayNavigatorPanel(
                    mode = state.mode,
                    selectedDayTitle = state.selectedDay?.title ?: "День не выбран",
                    selectedDate = state.selectedDate,
                    currentWeekType = state.currentWeekType,
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
                    onSelectWeekFilter = onSelectWeekFilter
                )
            }
        }

        item {
            if (state.isLoading && state.lessons.isEmpty()) {
                LessonTableSkeleton()
            } else if (timeSlots.isEmpty()) {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    EmptyStateBlock(
                        text = if (state.lessons.isEmpty()) {
                            "Занятий не найдено"
                        } else {
                            "На выбранный день и неделю пар нет"
                        }
                    )
                }
            } else {
                LessonTableCard(
                    timeSlots = timeSlots,
                    currentWeekType = if (state.mode == ScheduleMode.BY_DATE) {
                        state.selectedDateWeekType
                    } else {
                        state.currentWeekType
                    },
                    weekFilter = state.weekFilter,
                    date = state.selectedDate,
                    isDateMode = state.mode == ScheduleMode.BY_DATE,
                    noteMap = noteTextMap,
                    reminderMap = lessonEntryMap,
                    onAddOrEditNote = { lesson -> editingLesson = lesson },
                    onAddOrEditReminder = { lesson -> reminderLesson = lesson }
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 26.dp, end = 18.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Surface(
            modifier = Modifier
                .size(52.dp)
                .clickable { showNotesDialog = true },
            shape = CircleShape,
            color = NovsuBlue,
            border = BorderStroke(0.8.dp, NovsuBlue)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Notes,
                    contentDescription = "Все заметки",
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    if (showNotesDialog) {
        NotesOverviewDialog(
            notes = state.notes.filter { it.noteText.isNotBlank() },
            onDismiss = { showNotesDialog = false },
            onEdit = { noteId ->
                editingNoteId = noteId
                showNotesDialog = false
            }
        )
    }

    editingLesson?.let { lesson ->
        val note = noteTextMap[noteLessonKey(state.selectedDate, lesson.timeRange, lesson.weekType, lesson.subject, lesson.rawText)]
        LessonNoteDialog(
            lesson = lesson,
            note = note,
            canEdit = state.mode == ScheduleMode.BY_DATE && isLessonEditableNowOrFuture(state.selectedDate, lesson.timeRange),
            onDismiss = { editingLesson = null },
            onSave = { text ->
                onSaveLessonNote(lesson, text)
                editingLesson = null
            },
            onDelete = {
                onDeleteLessonNote(lesson)
                editingLesson = null
            }
        )
    }

    reminderLesson?.let { lesson ->
        val note = lessonEntryMap[noteLessonKey(state.selectedDate, lesson.timeRange, lesson.weekType, lesson.subject, lesson.rawText)]
        ReminderDialog(
            lesson = lesson,
            note = note,
            canEdit = state.mode == ScheduleMode.BY_DATE && isLessonEditableNowOrFuture(state.selectedDate, lesson.timeRange),
            onDismiss = { reminderLesson = null },
            onSave = { enabled, minutes ->
                onSetLessonReminder(lesson, enabled, minutes)
                reminderLesson = null
            }
        )
    }

    editingNoteId?.let { noteId ->
        val note = state.notes.firstOrNull { it.noteId == noteId }
        if (note != null) {
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
                }
            )
        }
    }
}

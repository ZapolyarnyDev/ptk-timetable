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

@Composable
private fun DayNavigatorPanel(
    mode: ScheduleMode,
    selectedDayTitle: String,
    selectedDate: LocalDate,
    currentWeekType: PtkCurrentWeekType,
    dayIndex: Int,
    totalDays: Int,
    canGoPrev: Boolean,
    canGoNext: Boolean,
    onSelectMode: (ScheduleMode) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onGoToToday: () -> Unit,
    availableDays: List<ScheduleDay>,
    selectedDay: ScheduleDay?,
    weekFilter: ScheduleWeekFilter,
    onSelectDay: (ScheduleDay) -> Unit,
    onSelectWeekFilter: (ScheduleWeekFilter) -> Unit
) {
    val context = LocalContext.current

    SectionCard {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ScheduleMode.entries, key = { it.name }) { item ->
                WeekChip(
                    selected = item == mode,
                    label = item.title,
                    icon = if (item == ScheduleMode.BY_DAY) Icons.Outlined.Schedule else Icons.Outlined.CalendarMonth,
                    onClick = { onSelectMode(item) }
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        if (mode == ScheduleMode.BY_DAY) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavArrowButton(
                    icon = Icons.AutoMirrored.Outlined.ArrowBack,
                    enabled = canGoPrev,
                    onClick = onPreviousDay
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = selectedDayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = InkPrimary,
                        fontFamily = HeadingFontFamily
                    )
                    if (totalDays > 0) {
                        Text(
                            text = "день ${dayIndex + 1} из $totalDays",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                }
                NavArrowButton(
                    icon = Icons.AutoMirrored.Outlined.ArrowForward,
                    enabled = canGoNext,
                    onClick = onNextDay
                )
            }

            Spacer(Modifier.height(10.dp))
            if (availableDays.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableDays, key = { it.name }) { day ->
                        WeekChip(
                            selected = day == selectedDay,
                            label = day.shortTitle,
                            icon = Icons.Outlined.Schedule,
                            onClick = { onSelectDay(day) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ScheduleWeekFilter.entries, key = { it.name }) { filter ->
                    WeekChip(
                        selected = filter == weekFilter,
                        label = filter.title,
                        icon = Icons.Outlined.Tune,
                        onClick = { onSelectWeekFilter(filter) }
                    )
                }
            }
            if (isWeekMismatchWarningNeeded(weekFilter, currentWeekType)) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Недели не совпадают: текущая ${weekTypeLabel(currentWeekType)}, " +
                        "показано расписание для ${weekFilter.title.lowercase(Locale.forLanguageTag("ru"))}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavArrowButton(
                    icon = Icons.AutoMirrored.Outlined.ArrowBack,
                    enabled = true,
                    onClick = onPreviousDate
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = formatDateTitle(selectedDate),
                        style = MaterialTheme.typography.titleMedium,
                        color = InkPrimary,
                        fontFamily = HeadingFontFamily
                    )
                    Text(
                        text = selectedDayTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                }
                NavArrowButton(
                    icon = Icons.AutoMirrored.Outlined.ArrowForward,
                    enabled = true,
                    onClick = onNextDate
                )
            }

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedActionButton(
                    text = "Сегодня",
                    onClick = onGoToToday
                )
                OutlinedActionButton(
                    text = "Выбрать дату",
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                onSelectDate(LocalDate.of(year, month + 1, dayOfMonth))
                            },
                            selectedDate.year,
                            selectedDate.monthValue - 1,
                            selectedDate.dayOfMonth
                        ).show()
                    }
                )
            }
        }
    }
}

@Composable
private fun LessonTableCard(
    timeSlots: List<TimeSlotUi>,
    currentWeekType: PtkCurrentWeekType,
    weekFilter: ScheduleWeekFilter,
    date: LocalDate,
    isDateMode: Boolean,
    noteMap: Map<String, ScheduleNoteItem>,
    reminderMap: Map<String, ScheduleNoteItem>,
    onAddOrEditNote: (ScheduleLessonItem) -> Unit,
    onAddOrEditReminder: (ScheduleLessonItem) -> Unit
) {
    SectionCard(padding = 0.dp) {
        timeSlots.forEachIndexed { index, slot ->
            LessonTableRow(
                slot = slot,
                currentWeekType = currentWeekType,
                weekFilter = weekFilter,
                date = date,
                isDateMode = isDateMode,
                noteMap = noteMap,
                reminderMap = reminderMap,
                onAddOrEditNote = onAddOrEditNote,
                onAddOrEditReminder = onAddOrEditReminder
            )
            if (index < timeSlots.lastIndex) {
                HorizontalDivider(thickness = 0.8.dp, color = BorderSubtle)
            }
        }
    }
}

@Composable
private fun LessonTableRow(
    slot: TimeSlotUi,
    currentWeekType: PtkCurrentWeekType,
    weekFilter: ScheduleWeekFilter,
    date: LocalDate,
    isDateMode: Boolean,
    noteMap: Map<String, ScheduleNoteItem>,
    reminderMap: Map<String, ScheduleNoteItem>,
    onAddOrEditNote: (ScheduleLessonItem) -> Unit,
    onAddOrEditReminder: (ScheduleLessonItem) -> Unit
) {
    val (startTime, endTime) = splitTimeRange(slot.timeRange)
    val timeIndent = 22.dp
    val isCurrentSlot = isDateMode && isCurrentLessonSlot(date, slot.timeRange)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 78.dp)
            .background(if (isCurrentSlot) NovsuBlueSoft.copy(alpha = 0.35f) else Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .width(96.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = NovsuBlue,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = startTime,
                    style = MaterialTheme.typography.titleSmall,
                    color = InkPrimary,
                    fontFamily = HeadingFontFamily
                )
            }

            Row {
                Spacer(modifier = Modifier.width(timeIndent))
                Text(
                    text = endTime,
                    style = MaterialTheme.typography.titleSmall,
                    color = InkPrimary,
                    fontFamily = HeadingFontFamily
                )
            }
        }

        Box(
            modifier = Modifier
                .width(1.dp)
                .heightIn(min = 78.dp)
                .background(BorderSubtle)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            if (slot.isSplitByWeek) {
                SplitWeekCell(
                    slot = slot,
                    currentWeekType = currentWeekType,
                    weekFilter = weekFilter,
                    date = date,
                    isDateMode = isDateMode,
                    noteMap = noteMap,
                    reminderMap = reminderMap,
                    onAddOrEditNote = onAddOrEditNote,
                    onAddOrEditReminder = onAddOrEditReminder
                )
            } else {
                LessonTextBlock(
                    lessons = slot.allLessons,
                    currentWeekType = currentWeekType,
                    date = date,
                    isDateMode = isDateMode,
                    noteMap = noteMap,
                    reminderMap = reminderMap,
                    onAddOrEditNote = onAddOrEditNote,
                    onAddOrEditReminder = onAddOrEditReminder
                )
            }
        }
    }
}

@Composable
private fun SplitWeekCell(
    slot: TimeSlotUi,
    currentWeekType: PtkCurrentWeekType,
    weekFilter: ScheduleWeekFilter,
    date: LocalDate,
    isDateMode: Boolean,
    noteMap: Map<String, ScheduleNoteItem>,
    reminderMap: Map<String, ScheduleNoteItem>,
    onAddOrEditNote: (ScheduleLessonItem) -> Unit,
    onAddOrEditReminder: (ScheduleLessonItem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        when {
            !isDateMode && weekFilter == ScheduleWeekFilter.UPPER -> {
                WeekHalfBlock(
                    title = "Верхняя",
                    lessons = slot.upperLessons,
                    weekType = PtkWeekType.UPPER,
                    currentWeekType = currentWeekType,
                    date = date,
                    isDateMode = isDateMode,
                    noteMap = noteMap,
                    reminderMap = reminderMap,
                    onAddOrEditNote = onAddOrEditNote,
                    onAddOrEditReminder = onAddOrEditReminder
                )
            }
            !isDateMode && weekFilter == ScheduleWeekFilter.LOWER -> {
                WeekHalfBlock(
                    title = "Нижняя",
                    lessons = slot.lowerLessons,
                    weekType = PtkWeekType.LOWER,
                    currentWeekType = currentWeekType,
                    date = date,
                    isDateMode = isDateMode,
                    noteMap = noteMap,
                    reminderMap = reminderMap,
                    onAddOrEditNote = onAddOrEditNote,
                    onAddOrEditReminder = onAddOrEditReminder
                )
            }
            else -> {
                WeekHalfBlock(
                    title = "Верхняя",
                    lessons = slot.upperLessons,
                    weekType = PtkWeekType.UPPER,
                    currentWeekType = currentWeekType,
                    date = date,
                    isDateMode = isDateMode,
                    noteMap = noteMap,
                    reminderMap = reminderMap,
                    onAddOrEditNote = onAddOrEditNote,
                    onAddOrEditReminder = onAddOrEditReminder
                )
                DashedHorizontalDivider()
                WeekHalfBlock(
                    title = "Нижняя",
                    lessons = slot.lowerLessons,
                    weekType = PtkWeekType.LOWER,
                    currentWeekType = currentWeekType,
                    date = date,
                    isDateMode = isDateMode,
                    noteMap = noteMap,
                    reminderMap = reminderMap,
                    onAddOrEditNote = onAddOrEditNote,
                    onAddOrEditReminder = onAddOrEditReminder
                )
            }
        }
    }
}

@Composable
private fun WeekHalfBlock(
    title: String,
    lessons: List<ScheduleLessonItem>,
    weekType: PtkWeekType,
    currentWeekType: PtkCurrentWeekType,
    date: LocalDate,
    isDateMode: Boolean,
    noteMap: Map<String, ScheduleNoteItem>,
    reminderMap: Map<String, ScheduleNoteItem>,
    onAddOrEditNote: (ScheduleLessonItem) -> Unit,
    onAddOrEditReminder: (ScheduleLessonItem) -> Unit
) {
    val isCurrent = weekTypeMatchesCurrent(weekType, currentWeekType)
    val titleAlpha = if (isCurrent) 1f else 0.45f

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = NovsuBlue.copy(alpha = titleAlpha),
            fontFamily = MainFontFamily
        )
        if (lessons.isEmpty()) {
            Text(
                text = "-",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSecondary.copy(alpha = titleAlpha)
            )
        } else {
            LessonTextBlock(
                lessons = lessons,
                currentWeekType = currentWeekType,
                date = date,
                isDateMode = isDateMode,
                noteMap = noteMap,
                reminderMap = reminderMap,
                onAddOrEditNote = onAddOrEditNote,
                onAddOrEditReminder = onAddOrEditReminder
            )
        }
    }
}

@Composable
private fun LessonTextBlock(
    lessons: List<ScheduleLessonItem>,
    currentWeekType: PtkCurrentWeekType,
    date: LocalDate,
    isDateMode: Boolean,
    noteMap: Map<String, ScheduleNoteItem>,
    reminderMap: Map<String, ScheduleNoteItem>,
    onAddOrEditNote: (ScheduleLessonItem) -> Unit,
    onAddOrEditReminder: (ScheduleLessonItem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lessons.forEachIndexed { index, lesson ->
            val isCurrent = weekTypeMatchesCurrent(lesson.weekType, currentWeekType)
            val textAlpha = if (isCurrent) 1f else 0.45f
            val note = noteMap[noteLessonKey(date, lesson.timeRange, lesson.weekType, lesson.subject, lesson.rawText)]
            val reminder = reminderMap[noteLessonKey(date, lesson.timeRange, lesson.weekType, lesson.subject, lesson.rawText)]
            val mainText = lesson.subject.ifBlank { lesson.rawText }
            val details = listOfNotNull(
                lesson.teacher?.takeIf { it.isNotBlank() },
                lesson.classroom?.takeIf { it.isNotBlank() }
            ).joinToString(", ")

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = mainText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkPrimary.copy(alpha = textAlpha),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onAddOrEditNote(lesson) },
                            enabled = isDateMode,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Notes,
                                contentDescription = "Заметка",
                                tint = if (note != null) NovsuBlue.copy(alpha = textAlpha) else InkSecondary.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { onAddOrEditReminder(lesson) },
                            enabled = isDateMode,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsActive,
                                contentDescription = "Напоминание",
                                tint = if (reminder?.reminderEnabled == true) NovsuBlue.copy(alpha = textAlpha) else InkSecondary.copy(alpha = 0.35f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                if (details.isNotBlank()) {
                    Text(
                        text = details,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary.copy(alpha = textAlpha)
                    )
                }
                if (note != null) {
                    Text(
                        text = "Заметка: ${note.noteText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NovsuBlue.copy(alpha = textAlpha),
                        maxLines = 2
                    )
                }
            }

            if (index < lessons.lastIndex) {
                HorizontalDivider(thickness = 0.8.dp, color = BorderSubtle.copy(alpha = 0.9f))
            }
        }
    }
}

@Composable
private fun LessonNoteDialog(
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
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Отмена", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.error)
                ) {
                    Text("Удалить", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            } else {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Отмена", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { onSave(noteText) },
            enabled = canEdit && noteText.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить", maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun NotesOverviewDialog(
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
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Закрыть")
        }
    }
}

@Composable
private fun ReminderDialog(
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
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("Отмена")
            }
            Button(
                onClick = { onSave(enabled, parsedMinutes ?: 10) },
                enabled = canEdit && (!enabled || parsedMinutes != null),
                modifier = Modifier.weight(1f)
            ) {
                Text("Сохранить")
            }
        }
    }
}

@Composable
private fun NoteEditByIdDialog(
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
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("Отмена", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f),
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить", maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun AppModalDialog(
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
                    .heightIn(min = 320.dp, max = 560.dp),
                color = White,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(0.8.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
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
                                color = InkSecondary
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        content()
                    }
                )
            }
        }
    }
}

private fun weekTypeMatchesCurrent(
    lessonWeekType: PtkWeekType,
    currentWeekType: PtkCurrentWeekType
): Boolean {
    return when (currentWeekType) {
        PtkCurrentWeekType.UNKNOWN -> true
        PtkCurrentWeekType.UPPER -> {
            lessonWeekType == PtkWeekType.UPPER || lessonWeekType == PtkWeekType.ALL
        }
        PtkCurrentWeekType.LOWER -> {
            lessonWeekType == PtkWeekType.LOWER || lessonWeekType == PtkWeekType.ALL
        }
    }
}

@Composable
private fun DashedHorizontalDivider(
    color: Color = BorderSubtle,
    stroke: Dp = 1.dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .drawBehind {
                val y = size.height / 2f
                drawLine(
                    color = color,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = stroke.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 6.dp.toPx()), 0f)
                )
            }
    )
}

@Composable
private fun HeaderPanel(
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    SectionCard {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(NovsuBlueSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = NovsuBlue)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = InkPrimary,
                    fontFamily = HeadingFontFamily
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSecondary
                )
            }
        }
    }
}

@Composable
private fun InfoPanel(
    content: @Composable () -> Unit
) {
    SectionCard(content = content)
}

@Composable
private fun SectionCard(
    padding: Dp = 14.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = White,
        border = BorderStroke(0.8.dp, BorderSubtle),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}

@Composable
private fun MetaRow(
    icon: ImageVector,
    text: String,
    highlight: Boolean = true
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (highlight) NovsuBlue else InkSecondary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (highlight) InkPrimary else InkSecondary
        )
    }
}

@Composable
private fun <T> SelectionListSection(
    title: String,
    items: List<T>,
    icon: (T) -> ImageVector,
    titleText: (T) -> String,
    subtitleText: (T) -> String,
    onClick: (T) -> Unit
) {
    SectionCard(padding = 0.dp) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = InkPrimary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            fontFamily = HeadingFontFamily
        )
        HorizontalDivider(thickness = 0.8.dp, color = BorderSubtle)

        items.forEachIndexed { index, item ->
            SelectionRow(
                icon = icon(item),
                title = titleText(item),
                subtitle = subtitleText(item),
                onClick = { onClick(item) }
            )
            if (index < items.lastIndex) {
                HorizontalDivider(thickness = 0.8.dp, color = BorderSubtle)
            }
        }
    }
}

@Composable
private fun SelectionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(NovsuBlueSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = NovsuBlue, modifier = Modifier.size(20.dp))
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = InkPrimary,
                fontFamily = HeadingFontFamily
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = InkSecondary
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = InkSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun EmptyStateBlock(text: String) {
    SectionCard {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = InkSecondary
        )
    }
}

@Composable
private fun SelectionListSkeleton(rows: Int) {
    SectionCard {
        repeat(rows) { index ->
            val firstWidth = if (index % 2 == 0) 0.42f else 0.58f
            val secondWidth = if (index % 2 == 0) 0.75f else 0.63f
            SkeletonBar(widthFraction = firstWidth, height = 14.dp)
            Spacer(Modifier.height(6.dp))
            SkeletonBar(widthFraction = secondWidth, height = 11.dp)
            if (index < rows - 1) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(thickness = 0.8.dp, color = BorderSubtle)
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = NovsuBlue, contentColor = White),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text = text, fontFamily = MainFontFamily)
    }
}

@Composable
private fun OutlinedActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        border = BorderStroke(0.8.dp, NovsuBlue),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = NovsuBlue)
    ) {
        Text(text = text, fontFamily = MainFontFamily)
    }
}

@Composable
private fun NavArrowButton(
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (enabled) NovsuBlueSoft else SurfaceMuted,
        border = BorderStroke(0.8.dp, if (enabled) NovsuBlue else BorderSubtle),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) NovsuBlue else InkSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun WeekChip(
    selected: Boolean,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontFamily = MainFontFamily) },
        leadingIcon = { Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = BorderSubtle,
            selectedBorderColor = NovsuBlue
        ),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = White,
            selectedContainerColor = NovsuBlueSoft,
            selectedLabelColor = NovsuBlue,
            labelColor = InkSecondary,
            iconColor = InkSecondary,
            selectedLeadingIconColor = NovsuBlue
        ),
        elevation = FilterChipDefaults.filterChipElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
    )
}

@Composable
private fun InlineLoading() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = NovsuBlue
        )
        Text(
            text = "Загрузка...",
            style = MaterialTheme.typography.bodySmall,
            color = InkSecondary
        )
    }
}

@Composable
private fun LessonTableSkeleton() {
    SectionCard {
        repeat(5) { index ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SkeletonBar(widthFraction = 0.2f, height = 14.dp)
                SkeletonBar(widthFraction = 0.65f, height = 14.dp)
            }
            Spacer(Modifier.height(8.dp))
            SkeletonBar(widthFraction = 0.78f, height = 11.dp)
            if (index < 4) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(thickness = 0.8.dp, color = BorderSubtle)
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun SkeletonBar(
    widthFraction: Float,
    height: Dp
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(MaterialTheme.shapes.small)
            .background(NovsuBlueSoft.copy(alpha = alpha))
    )
}

data class TimeSlotUi(
    val timeRange: String,
    val allLessons: List<ScheduleLessonItem>,
    val upperLessons: List<ScheduleLessonItem>,
    val lowerLessons: List<ScheduleLessonItem>
) {
    val isSplitByWeek: Boolean
        get() = upperLessons.isNotEmpty() || lowerLessons.isNotEmpty()
}

private fun buildTimeSlots(lessons: List<ScheduleLessonItem>): List<TimeSlotUi> {
    return lessons
        .groupBy { it.timeRange }
        .map { (timeRange, rows) ->
            TimeSlotUi(
                timeRange = timeRange,
                allLessons = rows.filter { it.weekType == PtkWeekType.ALL },
                upperLessons = rows.filter { it.weekType == PtkWeekType.UPPER },
                lowerLessons = rows.filter { it.weekType == PtkWeekType.LOWER }
            )
        }
        .sortedBy { lessonSortKey(it.timeRange) }
}

private fun filterLessons(state: ScheduleUiState): List<ScheduleLessonItem> {
    if (state.mode == ScheduleMode.BY_DATE) {
        return state.lessons
            .asSequence()
            .sortedBy { lessonSortKey(it.timeRange) }
            .toList()
    }

    val selectedDay = state.selectedDay ?: return emptyList()
    return state.lessons
        .asSequence()
        .filter { it.day == selectedDay }
        .filter { lesson -> lessonMatchesWeekFilter(lesson.weekType, state.weekFilter) }
        .sortedBy { lessonSortKey(it.timeRange) }
        .toList()
}

private fun lessonMatchesWeekFilter(weekType: PtkWeekType, filter: ScheduleWeekFilter): Boolean {
    return when (filter) {
        ScheduleWeekFilter.ALL -> true
        ScheduleWeekFilter.UPPER -> weekType == PtkWeekType.UPPER || weekType == PtkWeekType.ALL
        ScheduleWeekFilter.LOWER -> weekType == PtkWeekType.LOWER || weekType == PtkWeekType.ALL
    }
}

private fun lessonSortKey(timeRange: String): Int {
    val normalized = timeRange.replace('—', '-').replace('–', '-')
    val match = Regex("(\\d{1,2})[.:](\\d{2})").find(normalized) ?: return Int.MAX_VALUE
    val hours = match.groupValues[1].toIntOrNull() ?: return Int.MAX_VALUE
    val minutes = match.groupValues[2].toIntOrNull() ?: return Int.MAX_VALUE
    return hours * 60 + minutes
}

private fun splitTimeRange(timeRange: String): Pair<String, String> {
    val normalized = timeRange
        .replace('—', '-')
        .replace('–', '-')
        .replace(" ", "")
    val parts = normalized.split("-", limit = 2)
    val start = parts.getOrNull(0).orEmpty().ifBlank { timeRange }
    val end = parts.getOrNull(1).orEmpty().ifBlank { "" }
    return start to end
}

private fun noteLessonKey(
    date: LocalDate,
    timeRange: String,
    weekType: PtkWeekType,
    subject: String,
    rawText: String
): String {
    return listOf(
        date.toString(),
        timeRange.trim(),
        weekType.name,
        subject.trim(),
        rawText.trim().hashCode().toString()
    ).joinToString("|")
}

private fun isLessonEditableNowOrFuture(
    date: LocalDate,
    timeRange: String
): Boolean {
    val start = splitTimeRange(timeRange).first
    val match = Regex("(\\d{1,2})[.:](\\d{2})").find(start) ?: return false
    val h = match.groupValues[1].toIntOrNull() ?: return false
    val m = match.groupValues[2].toIntOrNull() ?: return false
    val startDateTime = runCatching { java.time.LocalDateTime.of(date, java.time.LocalTime.of(h, m)) }.getOrNull()
        ?: return false
    return !startDateTime.isBefore(java.time.LocalDateTime.now())
}

private fun isCurrentLessonSlot(
    date: LocalDate,
    timeRange: String
): Boolean {
    val now = java.time.LocalDateTime.now()
    if (now.toLocalDate() != date) return false
    val (startRaw, endRaw) = splitTimeRange(timeRange)
    val startMatch = Regex("(\\d{1,2})[.:](\\d{2})").find(startRaw) ?: return false
    val endMatch = Regex("(\\d{1,2})[.:](\\d{2})").find(endRaw) ?: return false
    val start = runCatching {
        java.time.LocalDateTime.of(
            date,
            java.time.LocalTime.of(startMatch.groupValues[1].toInt(), startMatch.groupValues[2].toInt())
        )
    }.getOrNull() ?: return false
    val end = runCatching {
        java.time.LocalDateTime.of(
            date,
            java.time.LocalTime.of(endMatch.groupValues[1].toInt(), endMatch.groupValues[2].toInt())
        )
    }.getOrNull() ?: return false
    return !now.isBefore(start) && now.isBefore(end)
}

private fun formatDateTitle(date: LocalDate): String {
    return DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.forLanguageTag("ru"))
        .format(date)
}

private fun weekTypeLabel(type: PtkCurrentWeekType): String = when (type) {
    PtkCurrentWeekType.UPPER -> "верхняя"
    PtkCurrentWeekType.LOWER -> "нижняя"
    PtkCurrentWeekType.UNKNOWN -> "не определена"
}

private fun isWeekMismatchWarningNeeded(
    selectedFilter: ScheduleWeekFilter,
    currentWeekType: PtkCurrentWeekType
): Boolean {
    if (selectedFilter == ScheduleWeekFilter.ALL) return false
    return when (currentWeekType) {
        PtkCurrentWeekType.UNKNOWN -> false
        PtkCurrentWeekType.UPPER -> selectedFilter == ScheduleWeekFilter.LOWER
        PtkCurrentWeekType.LOWER -> selectedFilter == ScheduleWeekFilter.UPPER
    }
}

private fun formatInstant(value: Instant): String {
    return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru"))
        .withZone(ZoneId.systemDefault())
        .format(value)
}

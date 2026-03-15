package io.github.zapolyarnydev.ptktimetable.ui.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zapolyarnydev.ptktimetable.data.model.PtkCurrentWeekType
import io.github.zapolyarnydev.ptktimetable.data.model.PtkGroupInfo
import io.github.zapolyarnydev.ptktimetable.data.model.PtkWeekType
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
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
    onSelectDay: (ScheduleDay) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onSelectWeekFilter: (ScheduleWeekFilter) -> Unit
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
        onSelectDay = onSelectDay,
        onPreviousDay = onPreviousDay,
        onNextDay = onNextDay,
        onSelectWeekFilter = onSelectWeekFilter
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
    onSelectDay: (ScheduleDay) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onSelectWeekFilter: (ScheduleWeekFilter) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(screenTitle(state)) })
        }
    ) { padding ->
        val showBlockingLoader = state.isLoading &&
            state.courses.isEmpty() &&
            state.courseGroups.isEmpty() &&
            state.lessons.isEmpty()

        when {
            showBlockingLoader -> LoadingState(padding)
            state.errorMessage != null &&
                state.courses.isEmpty() &&
                state.courseGroups.isEmpty() &&
                state.lessons.isEmpty() -> ErrorState(padding, state.errorMessage, onRetry)
            state.step == ScheduleStep.COURSE_SELECTION -> CourseSelectionState(
                padding = padding,
                state = state,
                onRefresh = onRefresh,
                onCourseSelect = onCourseSelect
            )
            state.step == ScheduleStep.GROUP_SELECTION -> GroupSelectionState(
                padding = padding,
                state = state,
                onRefresh = onRefresh,
                onBackToCourses = onBackToCourses,
                onGroupSelect = onGroupSelect
            )
            else -> ScheduleState(
                padding = padding,
                state = state,
                onRefresh = onRefresh,
                onBackToGroups = onBackToGroups,
                onSelectDay = onSelectDay,
                onPreviousDay = onPreviousDay,
                onNextDay = onNextDay,
                onSelectWeekFilter = onSelectWeekFilter
            )
        }
    }
}

@Composable
private fun LoadingState(padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    padding: PaddingValues,
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Button(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
            Text("Повторить")
        }
    }
}

@Composable
private fun CourseSelectionState(
    padding: PaddingValues,
    state: ScheduleUiState,
    onRefresh: () -> Unit,
    onCourseSelect: (CourseItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Добро пожаловать в PtkSchedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Выберите курс, чтобы увидеть доступные группы.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        item {
            Text(
                text = "Текущая неделя: ${weekTypeLabel(state.currentWeekType)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        item {
            state.groupsUpdatedAt?.let {
                Text(
                    text = "Обновлено: ${formatInstant(it)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onRefresh) {
                    Text("Обновить")
                }
                if (state.isLoading) {
                    Text(
                        text = "Загрузка...",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
        item {
            state.errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }

        if (state.courses.isEmpty() && !state.isLoading) {
            item {
                Text(
                    text = "Курсы не найдены",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            items(state.courses, key = { it.course }) { course ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCourseSelect(course) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = course.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Курс №${course.course}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
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
            .padding(padding),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onBackToCourses) {
                    Text("К курсам")
                }
                OutlinedButton(onClick = onRefresh) {
                    Text("Обновить")
                }
            }
        }
        item {
            Text(
                text = "Выбран курс: ${state.selectedCourse?.title ?: "-"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
        item {
            if (state.isLoading) {
                Text("Обновление...", style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            state.errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }

        if (state.courseGroups.isEmpty() && !state.isLoading) {
            item {
                Text("Для этого курса группы не найдены")
            }
        } else {
            items(state.courseGroups, key = { "${it.groupName}_${it.course}" }) { group ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGroupSelect(group) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Группа ${group.groupName}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = group.collegeName,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
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
    onSelectDay: (ScheduleDay) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onSelectWeekFilter: (ScheduleWeekFilter) -> Unit
) {
    val filteredLessons = filterLessons(state)
    val selectedDay = state.selectedDay
    val selectedDayIndex = state.availableDays.indexOf(selectedDay).takeIf { it >= 0 } ?: 0
    val canGoPrevious = selectedDayIndex > 0
    val canGoNext = selectedDayIndex < state.availableDays.lastIndex

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onBackToGroups) {
                    Text("К группам")
                }
                OutlinedButton(onClick = onRefresh) {
                    Text("Обновить")
                }
            }
        }
        item {
            state.scheduleUpdatedAt?.let {
                Text(
                    text = "Обновлено: ${formatInstant(it)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        item {
            state.errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onPreviousDay, enabled = canGoPrevious) {
                    Text("←")
                }
                Text(
                    text = selectedDay?.title ?: "День не выбран",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedButton(onClick = onNextDay, enabled = canGoNext) {
                    Text("→")
                }
            }
        }

        if (state.availableDays.isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.availableDays, key = { it.name }) { day ->
                        FilterChip(
                            selected = day == state.selectedDay,
                            onClick = { onSelectDay(day) },
                            label = { Text(day.shortTitle) }
                        )
                    }
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ScheduleWeekFilter.entries, key = { it.name }) { filter ->
                    FilterChip(
                        selected = state.weekFilter == filter,
                        onClick = { onSelectWeekFilter(filter) },
                        label = { Text(filter.title) }
                    )
                }
            }
        }

        if (filteredLessons.isEmpty()) {
            item {
                Text(
                    text = if (state.lessons.isEmpty()) {
                        "Занятий не найдено"
                    } else {
                        "На выбранный день и неделю пар нет"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            items(
                filteredLessons,
                key = { "${it.day}_${it.timeRange}_${it.rawText}_${it.weekType}" }
            ) { lesson ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = lesson.timeRange,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Неделя: ${lesson.weekType.titleRu}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = lesson.subject.ifBlank { lesson.rawText },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        lesson.teacher?.let {
                            Text(text = it, style = MaterialTheme.typography.bodyMedium)
                        }
                        lesson.classroom?.let {
                            Text(text = it, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

private fun screenTitle(state: ScheduleUiState): String = when (state.step) {
    ScheduleStep.COURSE_SELECTION -> "PtkSchedule"
    ScheduleStep.GROUP_SELECTION -> state.selectedCourse?.title ?: "Выбор группы"
    ScheduleStep.SCHEDULE -> state.selectedGroup?.let { "Группа ${it.groupName}" } ?: "Расписание"
}

private fun filterLessons(state: ScheduleUiState): List<ScheduleLessonItem> {
    val selectedDay = state.selectedDay ?: return emptyList()
    return state.lessons
        .asSequence()
        .filter { it.day == selectedDay }
        .filter { lesson -> lessonMatchesWeekFilter(lesson.weekType, state.weekFilter) }
        .sortedBy { lessonSortKey(it = it) }
        .toList()
}

private fun lessonMatchesWeekFilter(weekType: PtkWeekType, filter: ScheduleWeekFilter): Boolean {
    return when (filter) {
        ScheduleWeekFilter.ALL -> true
        ScheduleWeekFilter.UPPER -> weekType == PtkWeekType.UPPER || weekType == PtkWeekType.ALL
        ScheduleWeekFilter.LOWER -> weekType == PtkWeekType.LOWER || weekType == PtkWeekType.ALL
    }
}

private fun lessonSortKey(it: ScheduleLessonItem): Int {
    val normalized = it.timeRange.replace('—', '-').replace('–', '-')
    val match = Regex("(\\d{1,2})[.:](\\d{2})").find(normalized) ?: return Int.MAX_VALUE
    val hours = match.groupValues[1].toIntOrNull() ?: return Int.MAX_VALUE
    val minutes = match.groupValues[2].toIntOrNull() ?: return Int.MAX_VALUE
    return hours * 60 + minutes
}

private fun weekTypeLabel(type: PtkCurrentWeekType): String = when (type) {
    PtkCurrentWeekType.UPPER -> "верхняя"
    PtkCurrentWeekType.LOWER -> "нижняя"
    PtkCurrentWeekType.UNKNOWN -> "не определена"
}

private fun formatInstant(value: Instant): String {
    return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru"))
        .withZone(ZoneId.systemDefault())
        .format(value)
}

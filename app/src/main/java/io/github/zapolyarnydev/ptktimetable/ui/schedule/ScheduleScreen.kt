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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import io.github.zapolyarnydev.ptktimetable.data.model.PtkRawLesson

@Composable
fun ScheduleScreen(
    state: kotlinx.coroutines.flow.StateFlow<ScheduleUiState>,
    onRetry: () -> Unit,
    onGroupClick: (PtkGroupInfo) -> Unit,
    onBack: () -> Unit
) {
    val uiState by state.collectAsStateWithLifecycle()
    ScheduleScreenContent(
        state = uiState,
        onRetry = onRetry,
        onGroupClick = onGroupClick,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleScreenContent(
    state: ScheduleUiState,
    onRetry: () -> Unit,
    onGroupClick: (PtkGroupInfo) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.selectedGroup?.let { "Группа ${it.groupName}" } ?: "PtkSchedule"
                    )
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(padding)
            state.errorMessage != null -> ErrorState(padding, state.errorMessage, onRetry)
            state.selectedGroup == null -> GroupsState(
                padding = padding,
                groups = state.groups,
                weekType = state.currentWeekType,
                onGroupClick = onGroupClick
            )
            else -> LessonsState(
                padding = padding,
                lessons = state.lessons,
                onBack = onBack
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
private fun GroupsState(
    padding: PaddingValues,
    groups: List<PtkGroupInfo>,
    weekType: PtkCurrentWeekType,
    onGroupClick: (PtkGroupInfo) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Текущая неделя: ${weekTypeLabel(weekType)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
        items(groups, key = { "${it.groupName}_${it.course}" }) { group ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onGroupClick(group) }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Группа ${group.groupName}", style = MaterialTheme.typography.titleMedium)
                    Text(text = "${group.courseName} • ${group.collegeName}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun LessonsState(
    padding: PaddingValues,
    lessons: List<PtkRawLesson>,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Button(onClick = onBack) {
                Text("К группам")
            }
        }
        if (lessons.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Занятия не найдены")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lessons, key = { "${it.dayOfWeek}_${it.timeRange}_${it.rawText}_${it.weekType}" }) { lesson ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "${lesson.dayOfWeek}, ${lesson.timeRange}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Неделя: ${lesson.weekType.titleRu}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(text = lesson.rawText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

private fun weekTypeLabel(type: PtkCurrentWeekType): String = when (type) {
    PtkCurrentWeekType.UPPER -> "верхняя"
    PtkCurrentWeekType.LOWER -> "нижняя"
    PtkCurrentWeekType.UNKNOWN -> "не определена"
}

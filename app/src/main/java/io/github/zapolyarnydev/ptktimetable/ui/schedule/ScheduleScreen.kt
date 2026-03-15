package io.github.zapolyarnydev.ptktimetable.ui.schedule

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
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Groups
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
    Scaffold(containerColor = White) { padding ->
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
                onSelectDay = onSelectDay,
                onPreviousDay = onPreviousDay,
                onNextDay = onNextDay,
                onSelectWeekFilter = onSelectWeekFilter
            )
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
                MetaRow(
                    icon = Icons.Outlined.CalendarMonth,
                    text = "Текущая неделя: ${weekTypeLabel(state.currentWeekType)}"
                )
                state.groupsUpdatedAt?.let {
                    Spacer(Modifier.height(6.dp))
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
    onSelectDay: (ScheduleDay) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onSelectWeekFilter: (ScheduleWeekFilter) -> Unit
) {
    val filteredLessons = filterLessons(state)
    val timeSlots = buildTimeSlots(filteredLessons)
    val dayIndex = state.availableDays.indexOf(state.selectedDay).takeIf { it >= 0 } ?: 0
    val canGoPrev = dayIndex > 0
    val canGoNext = dayIndex < state.availableDays.lastIndex

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
                    selectedDayTitle = state.selectedDay?.title ?: "День не выбран",
                    dayIndex = dayIndex,
                    totalDays = state.availableDays.size,
                    canGoPrev = canGoPrev,
                    canGoNext = canGoNext,
                    onPreviousDay = onPreviousDay,
                    onNextDay = onNextDay,
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
                LessonTableCard(timeSlots = timeSlots)
            }
        }
    }
}

@Composable
private fun DayNavigatorPanel(
    selectedDayTitle: String,
    dayIndex: Int,
    totalDays: Int,
    canGoPrev: Boolean,
    canGoNext: Boolean,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    availableDays: List<ScheduleDay>,
    selectedDay: ScheduleDay?,
    weekFilter: ScheduleWeekFilter,
    onSelectDay: (ScheduleDay) -> Unit,
    onSelectWeekFilter: (ScheduleWeekFilter) -> Unit
) {
    SectionCard {
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
    }
}

@Composable
private fun LessonTableCard(timeSlots: List<TimeSlotUi>) {
    SectionCard(padding = 0.dp) {
        timeSlots.forEachIndexed { index, slot ->
            LessonTableRow(slot)
            if (index < timeSlots.lastIndex) {
                HorizontalDivider(thickness = 0.8.dp, color = BorderSubtle)
            }
        }
    }
}

@Composable
private fun LessonTableRow(slot: TimeSlotUi) {
    val (startTime, endTime) = splitTimeRange(slot.timeRange)
    val timeIndent = 22.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 78.dp)
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
                SplitWeekCell(slot = slot)
            } else {
                LessonTextBlock(lessons = slot.allLessons)
            }
        }
    }
}

@Composable
private fun SplitWeekCell(slot: TimeSlotUi) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        WeekHalfBlock(title = "Верхняя", lessons = slot.upperLessons)
        DashedHorizontalDivider()
        WeekHalfBlock(title = "Нижняя", lessons = slot.lowerLessons)
    }
}

@Composable
private fun WeekHalfBlock(
    title: String,
    lessons: List<ScheduleLessonItem>
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = NovsuBlue,
            fontFamily = MainFontFamily
        )
        if (lessons.isEmpty()) {
            Text(
                text = "-",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSecondary
            )
        } else {
            LessonTextBlock(lessons = lessons)
        }
    }
}

@Composable
private fun LessonTextBlock(lessons: List<ScheduleLessonItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lessons.forEachIndexed { index, lesson ->
            val mainText = lesson.subject.ifBlank { lesson.rawText }
            val details = listOfNotNull(
                lesson.teacher?.takeIf { it.isNotBlank() },
                lesson.classroom?.takeIf { it.isNotBlank() }
            ).joinToString(", ")

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = mainText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                if (details.isNotBlank()) {
                    Text(
                        text = details,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
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

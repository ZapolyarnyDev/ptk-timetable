package io.github.zapolyarnydev.ptktimetable.feature.schedule.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.github.zapolyarnydev.ptktimetable.core.ui.AppChoiceChip
import io.github.zapolyarnydev.ptktimetable.core.ui.NavArrowButton
import io.github.zapolyarnydev.ptktimetable.core.ui.formatDateTitle
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.ScheduleMode
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekFilter
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleDay
import io.github.zapolyarnydev.ptktimetable.ui.schedule.title
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppDimensions
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppIcons
import io.github.zapolyarnydev.ptktimetable.ui.theme.MaterialThemeAppColors
import java.time.LocalDate
import java.util.Locale

@Composable
internal fun DayNavigatorPanel(
    mode: ScheduleMode,
    selectedDayTitle: String,
    selectedDate: LocalDate,
    currentWeekType: WeekType?,
    weekMismatch: Boolean,
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
    weekFilter: WeekFilter,
    onSelectDay: (ScheduleDay) -> Unit,
    onSelectWeekFilter: (WeekFilter) -> Unit,
    groupTitle: String? = null,
    courseTitle: String? = null,
    onBack: (() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null,
    errorMessage: String? = null,
) {
    val context = LocalContext.current
    val colors = MaterialThemeAppColors

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (onBack != null || onRefresh != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(AppIcons.back, contentDescription = "К группам")
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = colors.accent)) {
                                append(groupTitle ?: "Группа не выбрана")
                            }
                            courseTitle?.takeIf { it.isNotBlank() }?.let {
                                append("  •  ")
                                withStyle(SpanStyle(color = colors.textSecondary)) { append(it) }
                            }
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary,
                    )
                }
                if (onRefresh != null) {
                    IconButton(onClick = onRefresh) {
                        Icon(AppIcons.refresh, contentDescription = "Обновить")
                    }
                }
            }
            errorMessage?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = colors.error)
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf(ScheduleMode.BY_DATE, ScheduleMode.BY_DAY), key = { it.name }) { item ->
                AppChoiceChip(
                    selected = item == mode,
                    label = item.title,
                    icon = if (item == ScheduleMode.BY_DAY) AppIcons.schedule else AppIcons.calendar,
                    onClick = { onSelectMode(item) },
                    containerColor = Color.Transparent,
                    selectedContainerColor = colors.accentMuted,
                    labelColor = colors.textSecondary,
                    selectedLabelColor = colors.accent,
                    iconColor = colors.textSecondary,
                    selectedLeadingIconColor = colors.accent,
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            NavArrowButton(
                icon = AppIcons.back,
                enabled = if (mode == ScheduleMode.BY_DAY) canGoPrev else true,
                onClick = if (mode == ScheduleMode.BY_DAY) onPreviousDay else onPreviousDate,
            )
            if (mode == ScheduleMode.BY_DATE) {
                TextButton(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, day -> onSelectDate(LocalDate.of(year, month + 1, day)) },
                            selectedDate.year,
                            selectedDate.monthValue - 1,
                            selectedDate.dayOfMonth,
                        ).show()
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).heightIn(min = AppDimensions.touchTarget),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatDateTitle(selectedDate),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                        )
                        Text(
                            text = selectedDayTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = selectedDayTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = if (totalDays > 0) "День ${dayIndex + 1} из $totalDays" else "День не выбран",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                    )
                }
            }
            NavArrowButton(
                icon = AppIcons.forward,
                enabled = if (mode == ScheduleMode.BY_DAY) canGoNext else true,
                onClick = if (mode == ScheduleMode.BY_DAY) onNextDay else onNextDate,
            )
        }

        if (mode == ScheduleMode.BY_DATE && selectedDate != LocalDate.now()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(
                    onClick = onGoToToday,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.heightIn(min = AppDimensions.touchTarget),
                ) { Text("Сегодня", color = colors.accent) }
            }
        }

        if (mode == ScheduleMode.BY_DAY) {
            if (availableDays.isNotEmpty()) {
                Text("День недели", style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableDays, key = { it.name }) { day ->
                        AppChoiceChip(
                            selected = day == selectedDay,
                            label = day.shortTitle,
                            icon = AppIcons.schedule,
                            onClick = { onSelectDay(day) },
                            containerColor = Color.Transparent,
                            selectedContainerColor = colors.accentMuted,
                            labelColor = colors.textSecondary,
                            selectedLabelColor = colors.accent,
                            iconColor = colors.textSecondary,
                            selectedLeadingIconColor = colors.accent,
                        )
                    }
                }
            }
            Text("Неделя", style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
            WeekSelector(selected = weekFilter, onSelect = onSelectWeekFilter)
            if (weekMismatch) {
                Text(
                    text = "Сейчас ${weekTypeLabel(
                        currentWeekType,
                    )} неделя, а выбрана ${weekFilter.title.lowercase(Locale.forLanguageTag("ru"))}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.warning,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        } else {
            Text(
                text = "Неделя на дату: ${weekTypeLabel(currentWeekType)}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
        }
    }
}

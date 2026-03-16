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
import io.github.zapolyarnydev.ptktimetable.ui.theme.InkMuted
import io.github.zapolyarnydev.ptktimetable.ui.theme.InkSecondary
import io.github.zapolyarnydev.ptktimetable.ui.theme.MainFontFamily
import io.github.zapolyarnydev.ptktimetable.ui.theme.NovsuBlue
import io.github.zapolyarnydev.ptktimetable.ui.theme.NovsuBlueDark
import io.github.zapolyarnydev.ptktimetable.ui.theme.NovsuBlueSoft
import io.github.zapolyarnydev.ptktimetable.ui.theme.SurfaceBlueTint
import io.github.zapolyarnydev.ptktimetable.ui.theme.SurfaceMuted
import io.github.zapolyarnydev.ptktimetable.ui.theme.White
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


@Composable
internal fun DayNavigatorPanel(
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
                            color = InkMuted
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
                            onClick = { onSelectDay(day) },
                            containerColor = NovsuBlueSoft.copy(alpha = 0.45f),
                            selectedContainerColor = NovsuBlueSoft,
                            labelColor = NovsuBlueDark,
                            selectedLabelColor = NovsuBlueDark,
                            iconColor = NovsuBlueDark,
                            selectedLeadingIconColor = NovsuBlueDark
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceBlueTint)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Недели не совпадают: текущая ${weekTypeLabel(currentWeekType)}, " +
                            "показано расписание для ${weekFilter.title.lowercase(Locale.forLanguageTag("ru"))}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
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
                        color = InkMuted
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
                    onClick = onGoToToday,
                    modifier = Modifier.weight(1f)
                )
                OutlinedActionButton(
                    text = "Выбрать дату",
                    modifier = Modifier.weight(1f),
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

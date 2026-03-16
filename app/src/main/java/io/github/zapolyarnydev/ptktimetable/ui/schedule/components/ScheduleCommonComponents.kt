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
import androidx.compose.foundation.border
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
import io.github.zapolyarnydev.ptktimetable.ui.theme.BorderStrong
import io.github.zapolyarnydev.ptktimetable.ui.theme.HeadingFontFamily
import io.github.zapolyarnydev.ptktimetable.ui.theme.InkPrimary
import io.github.zapolyarnydev.ptktimetable.ui.theme.InkMuted
import io.github.zapolyarnydev.ptktimetable.ui.theme.InkSecondary
import io.github.zapolyarnydev.ptktimetable.ui.theme.MainFontFamily
import io.github.zapolyarnydev.ptktimetable.ui.theme.NovsuBlue
import io.github.zapolyarnydev.ptktimetable.ui.theme.NovsuBlueDark
import io.github.zapolyarnydev.ptktimetable.ui.theme.NovsuBlueSoft
import io.github.zapolyarnydev.ptktimetable.ui.theme.NovsuBlueSoftStrong
import io.github.zapolyarnydev.ptktimetable.ui.theme.SurfaceMuted
import io.github.zapolyarnydev.ptktimetable.ui.theme.SurfaceBlueTint
import io.github.zapolyarnydev.ptktimetable.ui.theme.White
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun HeaderPanel(
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    SectionCard {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(NovsuBlueSoftStrong)
                    .drawBehind {
                        drawCircle(
                            color = NovsuBlue.copy(alpha = 0.18f),
                            radius = size.minDimension / 2f,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = NovsuBlueDark, modifier = Modifier.size(20.dp))
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
internal fun InfoPanel(
    content: @Composable () -> Unit
) {
    SectionCard(content = content)
}

@Composable
internal fun SectionCard(
    padding: Dp = 14.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = White,
        border = BorderStroke(0.9.dp, BorderSubtle),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}

@Composable
internal fun MetaRow(
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
internal fun <T> SelectionListSection(
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
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
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
internal fun SelectionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NovsuBlueSoft.copy(alpha = 0.7f))
                .border(1.dp, BorderStrong, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = NovsuBlueDark, modifier = Modifier.size(18.dp))
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
                color = InkMuted
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
internal fun EmptyStateBlock(text: String) {
    SectionCard {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = InkSecondary
        )
    }
}

@Composable
internal fun SelectionListSkeleton(rows: Int) {
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
internal fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = NovsuBlueDark, contentColor = White),
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(horizontal = 17.dp, vertical = 11.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp, disabledElevation = 0.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text = text, style = MaterialTheme.typography.labelLarge, fontFamily = MainFontFamily)
    }
}

@Composable
internal fun OutlinedActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        border = BorderStroke(1.dp, BorderSubtle),
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(horizontal = 17.dp, vertical = 11.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp, disabledElevation = 0.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = NovsuBlueDark,
            containerColor = NovsuBlueSoft.copy(alpha = 0.55f),
            disabledContentColor = InkMuted
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, fontFamily = MainFontFamily)
    }
}

@Composable
internal fun NavArrowButton(
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = White,
        border = BorderStroke(1.dp, if (enabled) BorderStrong else BorderSubtle),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(42.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) NovsuBlueDark else InkSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
internal fun WeekChip(
    selected: Boolean,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    containerColor: Color = White,
    selectedContainerColor: Color = NovsuBlueSoftStrong,
    labelColor: Color = InkSecondary,
    selectedLabelColor: Color = InkPrimary,
    iconColor: Color = InkSecondary,
    selectedLeadingIconColor: Color = NovsuBlueDark,
    borderColor: Color = BorderSubtle,
    selectedBorderColor: Color = NovsuBlueDark
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                label,
                fontFamily = MainFontFamily,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        },
        leadingIcon = { Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = borderColor,
            selectedBorderColor = selectedBorderColor
        ),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = containerColor,
            selectedContainerColor = selectedContainerColor,
            selectedLabelColor = selectedLabelColor,
            labelColor = labelColor,
            iconColor = iconColor,
            selectedLeadingIconColor = selectedLeadingIconColor
        ),
        elevation = FilterChipDefaults.filterChipElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
    )
}

@Composable
internal fun InlineLoading() {
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
internal fun LessonTableSkeleton() {
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
internal fun SkeletonBar(
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


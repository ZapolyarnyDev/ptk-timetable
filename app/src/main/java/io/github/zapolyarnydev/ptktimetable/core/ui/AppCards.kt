package io.github.zapolyarnydev.ptktimetable.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppDimensions
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppIcons
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppShapes
import io.github.zapolyarnydev.ptktimetable.ui.theme.MaterialThemeAppColors

@Composable
internal fun HeaderPanel(title: String, subtitle: String, icon: ImageVector) {
    val colors = MaterialThemeAppColors
    TransparentSection(padding = 0.dp) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.padding(top = 5.dp).size(22.dp),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.textPrimary,
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
internal fun InfoPanel(content: @Composable () -> Unit) = TransparentSection(content = content)

@Composable
internal fun TransparentSection(
    modifier: Modifier = Modifier,
    padding: Dp = AppDimensions.sectionPadding,
    content: @Composable () -> Unit,
) {
    AnimatedReveal {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(padding),
            content = { content() },
        )
    }
}

@Composable
internal fun TonalSection(
    modifier: Modifier = Modifier,
    padding: Dp = AppDimensions.sectionPadding,
    color: Color = MaterialThemeAppColors.surfaceMuted,
    shape: androidx.compose.ui.graphics.Shape = AppShapes.medium,
    content: @Composable () -> Unit,
) {
    AnimatedReveal {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .background(color)
                .padding(padding),
            content = { content() },
        )
    }
}

@Composable
internal fun SectionCard(padding: Dp = AppDimensions.sectionPadding, content: @Composable () -> Unit) =
    TonalSection(padding = padding, content = content)

@Composable
internal fun MetaRow(icon: ImageVector, text: String, highlight: Boolean = true) {
    val colors = MaterialThemeAppColors
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (highlight) colors.accent else colors.textSecondary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (highlight) colors.textPrimary else colors.textSecondary,
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
    onClick: (T) -> Unit,
) {
    val colors = MaterialThemeAppColors
    TransparentSection(padding = 0.dp) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = AppDimensions.screenHorizontalPadding,
                vertical = AppDimensions.compactSpacing,
            ),
        )
        items.forEachIndexed { index, item ->
            SelectionRow(
                icon = icon(item),
                title = titleText(item),
                subtitle = subtitleText(item),
                onClick = { onClick(item) },
            )
            if (index < items.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = AppDimensions.screenHorizontalPadding),
                    color = colors.divider.copy(alpha = 0.55f),
                )
            }
        }
    }
}

@Composable
internal fun SelectionRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    val colors = MaterialThemeAppColors
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val rowColor by animateColorAsState(
        targetValue = when {
            pressed -> colors.accentMuted
            hovered -> colors.surfaceMuted
            else -> Color.Transparent
        },
        animationSpec = tween(120),
        label = "selectionRowColor",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowColor)
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
            )
            .heightIn(min = AppDimensions.listRowMinHeight)
            .padding(horizontal = AppDimensions.screenHorizontalPadding, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.padding(top = 2.dp).size(20.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
        }
        Icon(
            AppIcons.chevron,
            contentDescription = "Открыть",
            tint = colors.textSecondary,
            modifier = Modifier.padding(top = 2.dp).size(20.dp),
        )
    }
}

@Composable
internal fun EmptyStateBlock(text: String) {
    val colors = MaterialThemeAppColors
    TonalSection {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                AppIcons.schedule,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(26.dp),
            )
            Text(text, style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary)
        }
    }
}

@Composable
internal fun SelectionListSkeleton(rows: Int) {
    TransparentSection(padding = 0.dp) {
        repeat(rows) { index ->
            Row(
                modifier = Modifier.padding(
                    horizontal = AppDimensions.screenHorizontalPadding,
                    vertical = 14.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SkeletonBar(widthFraction = 0.06f, height = 20.dp)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonBar(widthFraction = if (index % 2 == 0) 0.48f else 0.62f, height = 15.dp)
                    SkeletonBar(widthFraction = 0.75f, height = 11.dp)
                }
            }
            if (index < rows - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = AppDimensions.screenHorizontalPadding),
                    color = MaterialThemeAppColors.divider.copy(alpha = 0.55f),
                )
            }
        }
    }
}

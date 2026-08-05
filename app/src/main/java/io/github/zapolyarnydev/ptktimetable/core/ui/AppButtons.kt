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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
internal fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource, enabled)
    Button(
        modifier = modifier
            .heightIn(min = AppDimensions.touchTarget)
            .graphicsLayerScale(scale),
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = AppShapes.pill,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        colors = ButtonDefaults.buttonColors(),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
internal fun OutlinedActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource, enabled)
    Button(
        modifier = modifier
            .heightIn(min = AppDimensions.touchTarget)
            .graphicsLayerScale(scale),
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = AppShapes.pill,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        colors = ButtonDefaults.filledTonalButtonColors(),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
    ) { Text(text, style = MaterialTheme.typography.labelLarge) }
}

@Composable
internal fun NavArrowButton(icon: ImageVector, enabled: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource, enabled)
    IconButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = Modifier.size(AppDimensions.touchTarget).graphicsLayerScale(scale),
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (enabled) MaterialThemeAppColors.accent else MaterialThemeAppColors.textSecondary,
        )
    }
}

@Composable
internal fun AppChoiceChip(
    selected: Boolean,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    containerColor: Color = MaterialThemeAppColors.surface,
    selectedContainerColor: Color = MaterialThemeAppColors.accentMuted,
    labelColor: Color = MaterialThemeAppColors.textSecondary,
    selectedLabelColor: Color = MaterialThemeAppColors.textPrimary,
    iconColor: Color = MaterialThemeAppColors.textSecondary,
    selectedLeadingIconColor: Color = MaterialThemeAppColors.accent,
    borderColor: Color = MaterialThemeAppColors.divider,
    selectedBorderColor: Color = MaterialThemeAppColors.accent,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource)
    FilterChip(
        modifier = Modifier.graphicsLayerScale(scale),
        selected = selected,
        onClick = onClick,
        interactionSource = interactionSource,
        label = { Text(label, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp)) },
        border = null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = containerColor,
            selectedContainerColor = selectedContainerColor,
            labelColor = labelColor,
            selectedLabelColor = selectedLabelColor,
            iconColor = iconColor,
            selectedLeadingIconColor = selectedLeadingIconColor,
        ),
    )
}

@Composable
internal fun rememberPressScale(interactionSource: MutableInteractionSource, enabled: Boolean = true): Float {
    val pressed by interactionSource.collectIsPressedAsState()
    return animateFloatAsState(
        targetValue = if (enabled && pressed) 0.975f else 1f,
        animationSpec = tween(90),
        label = "pressScale",
    ).value
}

private fun Modifier.graphicsLayerScale(scale: Float): Modifier = this.graphicsLayer {
    scaleX = scale
    scaleY = scale
}

@Composable
internal fun AnimatedReveal(key: Any? = Unit, content: @Composable () -> Unit) {
    var visible by remember(key) { mutableStateOf(false) }
    LaunchedEffect(key) { visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 14 },
        exit = fadeOut(tween(120)),
        label = "animatedReveal",
    ) { content() }
}

@Composable
internal fun OutlinedIconActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    active: Boolean = false,
    tint: Color = MaterialThemeAppColors.accent,
    inactiveTint: Color = MaterialThemeAppColors.textSecondary,
    size: Dp = 32.dp,
    iconSize: Dp = 17.dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) 0.92f else 1f, tween(90), label = "iconActionScale")
    Surface(
        modifier = modifier
            .size(size.coerceAtLeast(AppDimensions.touchTarget))
            .graphicsLayerScale(scale)
            .clickable(enabled = enabled, interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = CircleShape,
        color = when {
            !enabled -> MaterialThemeAppColors.surface
            active -> MaterialThemeAppColors.accentMuted
            else -> MaterialThemeAppColors.surfaceMuted
        },
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = if (enabled) tint else inactiveTint,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

@Composable
internal fun BorderlessIconActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    active: Boolean = false,
    activeTint: Color = MaterialThemeAppColors.accent,
    inactiveTint: Color = MaterialThemeAppColors.textSecondary,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(AppDimensions.touchTarget),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (active) activeTint else inactiveTint,
            modifier = Modifier.size(20.dp),
        )
    }
}

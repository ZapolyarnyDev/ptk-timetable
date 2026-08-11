package io.github.zapolyarnydev.ptktimetable.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun PtkTheme(settings: AppThemeSettings = ThemeManager.defaultSettings, content: @Composable () -> Unit) {
    val darkTheme = ThemeManager.isDark(settings, isSystemInDarkTheme())
    val palette = ThemeManager.palette(settings, darkTheme)
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = palette.onAccent,
            primaryContainer = palette.accentMuted,
            onPrimaryContainer = palette.textPrimary,
            secondary = palette.accent,
            onSecondary = palette.onAccent,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceMuted,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.divider,
            outlineVariant = palette.divider,
            error = palette.error,
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = palette.onAccent,
            primaryContainer = palette.accentMuted,
            onPrimaryContainer = palette.textPrimary,
            secondary = palette.accent,
            onSecondary = palette.onAccent,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceMuted,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.divider,
            outlineVariant = palette.divider,
            error = palette.error,
        )
    }

    CompositionLocalProvider(LocalAppColors provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PtkTypography,
            shapes = AppShapes.material,
            content = content,
        )
    }
}

val ColorScheme.cardBorder: androidx.compose.ui.graphics.Color
    get() = outlineVariant

package io.github.zapolyarnydev.ptktimetable.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun PtkTheme(settings: AppThemeSettings = ThemeManager.defaultSettings, content: @Composable () -> Unit) {
    val darkTheme = ThemeManager.isDark(settings, isSystemInDarkTheme())
    val palette = ThemeManager.palette(settings, darkTheme)
    ApplySystemBarAppearance(palette)
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

@Composable
private fun ApplySystemBarAppearance(palette: AppColors) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = palette.background.toArgb()
            window.navigationBarColor = palette.background.toArgb()
            val lightSystemBars = palette.background.luminance() > 0.5f
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = lightSystemBars
                isAppearanceLightNavigationBars = lightSystemBars
            }
        }
    }
}

val ColorScheme.cardBorder: androidx.compose.ui.graphics.Color
    get() = outlineVariant

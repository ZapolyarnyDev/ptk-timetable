package io.github.zapolyarnydev.ptktimetable.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import io.github.zapolyarnydev.ptktimetable.data.preferences.AppearancePreferences

enum class AppThemeMode(val storageValue: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark"),
    ;

    companion object {
        fun fromStorageValue(value: String?): AppThemeMode = entries.firstOrNull { it.storageValue == value } ?: LIGHT
    }
}

data class AppThemeSettings(
    val mode: AppThemeMode = AppThemeMode.LIGHT,
    val primaryTextOverride: Color? = null,
    val secondaryTextOverride: Color? = null,
    val backgroundOverride: Color? = null,
    val accentOverride: Color? = null,
    val secondarySurfaceOpacity: Float = AppearancePreferences.DEFAULT_SECONDARY_SURFACE_OPACITY,
)

fun AppearancePreferences.toThemeSettings(): AppThemeSettings = AppThemeSettings(
    mode = themeMode,
    primaryTextOverride = primaryTextColorArgb?.toComposeColor(),
    secondaryTextOverride = secondaryTextColorArgb?.toComposeColor(),
    backgroundOverride = backgroundColorArgb?.toComposeColor(),
    accentOverride = accentColorArgb?.toComposeColor(),
    secondarySurfaceOpacity = secondarySurfaceOpacity,
)

private fun Long.toComposeColor(): Color = Color(toInt())

object ThemeManager {
    val defaultSettings = AppThemeSettings()

    fun isDark(settings: AppThemeSettings, systemIsDark: Boolean): Boolean = when (settings.mode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> systemIsDark
    }

    fun palette(settings: AppThemeSettings, dark: Boolean): AppColors {
        val base = if (dark) DarkAppColors else LightAppColors
        val accent = settings.accentOverride ?: base.accent
        return base.copy(
            background = settings.backgroundOverride ?: base.background,
            surfaceMuted = base.surfaceMuted.copy(alpha = settings.secondarySurfaceOpacity.coerceIn(0f, 1f)),
            textPrimary = settings.primaryTextOverride ?: base.textPrimary,
            textSecondary = settings.secondaryTextOverride ?: base.textSecondary,
            accent = accent,
            onAccent = settings.accentOverride?.let {
                if (accent.luminance() > 0.5f) Color.Black else Color.White
            } ?: base.onAccent,
            accentMuted = settings.accentOverride?.copy(alpha = 0.16f) ?: base.accentMuted,
            currentLesson = settings.accentOverride?.copy(alpha = 0.16f) ?: base.currentLesson,
        )
    }
}

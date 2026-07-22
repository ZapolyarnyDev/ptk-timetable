package io.github.zapolyarnydev.ptktimetable.ui.theme

import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
    LIGHT,
    DARK,
    AMOLED,
    SYSTEM,
}

data class AppThemeSettings(
    val mode: AppThemeMode = AppThemeMode.LIGHT,
    val useDynamicColors: Boolean = false,
    val accentOverride: Color? = null,
)

object ThemeManager {
    val defaultSettings = AppThemeSettings()

    fun isDark(settings: AppThemeSettings, systemIsDark: Boolean): Boolean = when (settings.mode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK, AppThemeMode.AMOLED -> true
        AppThemeMode.SYSTEM -> systemIsDark
    }

    fun palette(settings: AppThemeSettings, dark: Boolean): AppColors = when {
        settings.mode == AppThemeMode.AMOLED -> DarkAppColors.copy(
            canvas = Color.Black,
            surfaceElevated = Color(0xFF050608),
            surfaceSoft = Color(0xFF111318),
        )

        dark -> DarkAppColors

        else -> LightAppColors
    }
}

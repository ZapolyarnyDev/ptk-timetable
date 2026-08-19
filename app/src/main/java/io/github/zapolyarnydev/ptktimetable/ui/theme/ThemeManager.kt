package io.github.zapolyarnydev.ptktimetable.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
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
    val primaryText: Color? = null,
    val secondaryText: Color? = null,
    val background: Color? = null,
    val accent: Color? = null,
)

fun AppearancePreferences.toThemeSettings(): AppThemeSettings = AppThemeSettings(
    mode = themeMode,
    primaryText = primaryTextColorArgb?.toComposeColor(),
    secondaryText = secondaryTextColorArgb?.toComposeColor(),
    background = backgroundColorArgb?.toComposeColor(),
    accent = accentColorArgb?.toComposeColor(),
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
        val background = settings.background?.opaqueOver(base.background) ?: base.background
        val accent = settings.accent?.opaqueOver(base.accent) ?: base.accent
        return base.copy(
            background = background,
            surface = blend(background, base.surface, SURFACE_BLEND),
            surfaceMuted = blend(background, base.surfaceMuted, MUTED_SURFACE_BLEND),
            textPrimary = settings.primaryText?.opaqueOver(base.textPrimary) ?: base.textPrimary,
            textSecondary = settings.secondaryText?.opaqueOver(base.textSecondary) ?: base.textSecondary,
            accent = accent,
            onAccent = readableOn(accent),
            accentMuted = blend(background, accent, ACCENT_MUTED_BLEND),
            currentLesson = blend(background, accent, CURRENT_LESSON_BLEND),
            divider = blend(background, base.divider, DIVIDER_BLEND),
        )
    }

    private fun Color.opaqueOver(fallback: Color): Color = copy(alpha = alpha.coerceIn(0f, 1f)).compositeOver(fallback)

    private fun blend(background: Color, foreground: Color, alpha: Float): Color =
        foreground.copy(alpha = alpha).compositeOver(background)

    private fun readableOn(background: Color): Color = if (contrastRatio(Color.White, background) >=
        contrastRatio(Color.Black, background)
    ) {
        Color.White
    } else {
        Color.Black
    }

    private fun contrastRatio(first: Color, second: Color): Float {
        val firstLuminance = relativeLuminance(first)
        val secondLuminance = relativeLuminance(second)
        return (maxOf(firstLuminance, secondLuminance) + 0.05f) / (minOf(firstLuminance, secondLuminance) + 0.05f)
    }

    private fun relativeLuminance(color: Color): Float =
        (0.2126f * linear(color.red)) + (0.7152f * linear(color.green)) + (0.0722f * linear(color.blue))

    private fun linear(component: Float): Float =
        if (component <= 0.04045f) component / 12.92f else ((component + 0.055f) / 1.055f).let { it * it * it }

    private const val SURFACE_BLEND = 0.82f
    private const val MUTED_SURFACE_BLEND = 0.68f
    private const val ACCENT_MUTED_BLEND = 0.16f
    private const val CURRENT_LESSON_BLEND = 0.22f
    private const val DIVIDER_BLEND = 0.72f
}

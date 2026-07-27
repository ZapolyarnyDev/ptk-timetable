package io.github.zapolyarnydev.ptktimetable.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val background: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val onAccent: Color,
    val accentMuted: Color,
    val divider: Color,
    val currentLesson: Color,
    val warning: Color,
    val error: Color,
)

internal val LightAppColors = AppColors(
    background = Color(0xFFF7F8FC),
    surface = Color(0xFFFFFFFF),
    surfaceMuted = Color(0xFFF0F3F9),
    textPrimary = Color(0xFF171A22),
    textSecondary = Color(0xFF5E6576),
    accent = Color(0xFF315FEA),
    onAccent = Color.White,
    accentMuted = Color(0xFFE8EEFF),
    divider = Color(0xFFDDE2EC),
    currentLesson = Color(0xFFE8EEFF),
    warning = Color(0xFF8A5A00),
    error = Color(0xFFBA1A1A),
)

internal val DarkAppColors = AppColors(
    background = Color(0xFF111318),
    surface = Color(0xFF1B1D24),
    surfaceMuted = Color(0xFF242730),
    textPrimary = Color(0xFFE5E6EE),
    textSecondary = Color(0xFFBFC4D0),
    accent = Color(0xFFB6C4FF),
    onAccent = Color(0xFF172B70),
    accentMuted = Color(0xFF27366E),
    divider = Color(0xFF454852),
    currentLesson = Color(0xFF27366E),
    warning = Color(0xFFFFB95F),
    error = Color(0xFFFFB4AB),
)

object AppOpacity {
    const val TRANSPARENT_SECTION = 0f
    const val SUBTLE_SURFACE = 0.72f
    const val DISABLED = 0.38f
}

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

val MaterialThemeAppColors: AppColors
    @Composable get() = LocalAppColors.current

val MaterialTheme.appColors: AppColors
    @Composable get() = LocalAppColors.current

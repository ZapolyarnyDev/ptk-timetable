package io.github.zapolyarnydev.ptktimetable.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val brand: Color,
    val brandStrong: Color,
    val brandContainer: Color,
    val canvas: Color,
    val surfaceElevated: Color,
    val surfaceSoft: Color,
    val onSurfaceMuted: Color,
    val currentLesson: Color,
    val currentLessonContainer: Color,
    val nextLesson: Color,
    val nextLessonContainer: Color,
    val success: Color,
    val warning: Color,
)

internal val LightAppColors = AppColors(
    brand = Color(0xFF315FEA),
    brandStrong = Color(0xFF2144B9),
    brandContainer = Color(0xFFE8EEFF),
    canvas = Color(0xFFF7F8FC),
    surfaceElevated = Color(0xFFFFFFFF),
    surfaceSoft = Color(0xFFF0F3F9),
    onSurfaceMuted = Color(0xFF5E6576),
    currentLesson = Color(0xFF315FEA),
    currentLessonContainer = Color(0xFFE8EEFF),
    nextLesson = Color(0xFF315FEA),
    nextLessonContainer = Color(0xFFF0F3FF),
    success = Color(0xFF315FEA),
    warning = Color(0xFF315FEA),
)

internal val DarkAppColors = AppColors(
    brand = Color(0xFFB6C4FF),
    brandStrong = Color(0xFFDCE2FF),
    brandContainer = Color(0xFF27366E),
    canvas = Color(0xFF111318),
    surfaceElevated = Color(0xFF1B1D24),
    surfaceSoft = Color(0xFF242730),
    onSurfaceMuted = Color(0xFFBFC4D0),
    currentLesson = Color(0xFFB6C4FF),
    currentLessonContainer = Color(0xFF27366E),
    nextLesson = Color(0xFFB6C4FF),
    nextLessonContainer = Color(0xFF20253D),
    success = Color(0xFFB6C4FF),
    warning = Color(0xFFB6C4FF),
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

val MaterialThemeAppColors: AppColors
    @Composable get() = LocalAppColors.current

val MaterialTheme.appColors: AppColors
    @Composable get() = LocalAppColors.current

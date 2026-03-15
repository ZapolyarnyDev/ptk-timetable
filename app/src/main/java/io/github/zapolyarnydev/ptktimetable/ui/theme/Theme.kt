package io.github.zapolyarnydev.ptktimetable.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AppColorScheme: ColorScheme = lightColorScheme(
    primary = NovsuBlue,
    onPrimary = White,
    secondary = NovsuBlueSoft,
    onSecondary = NovsuBlue,
    tertiary = NovsuBlue,
    onTertiary = White,
    background = White,
    onBackground = InkPrimary,
    surface = White,
    onSurface = InkPrimary,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = InkSecondary,
    outline = BorderSubtle,
    error = ErrorRed,
    onError = White
)

private val AppShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
)

@Composable
fun PtkTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = PtkTypography,
        shapes = AppShapes,
        content = content
    )
}

val ColorScheme.cardBorder: Color
    get() = outline


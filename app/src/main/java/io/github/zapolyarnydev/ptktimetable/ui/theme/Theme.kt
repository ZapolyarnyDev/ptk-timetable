package io.github.zapolyarnydev.ptktimetable.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

@Composable
fun PtkTheme(settings: AppThemeSettings = ThemeManager.defaultSettings, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val darkTheme = ThemeManager.isDark(settings, isSystemInDarkTheme())
    val palette = ThemeManager.palette(settings, darkTheme)
    val colorScheme = when {
        settings.useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> {
            dynamicDarkColorScheme(context)
        }

        settings.useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme(
            primary = palette.brand,
            onPrimary = ColorTokens.darkOnBrand,
            primaryContainer = palette.brandContainer,
            onPrimaryContainer = ColorTokens.darkOnBrandContainer,
            secondary = palette.brand,
            onSecondary = ColorTokens.darkOnBrand,
            background = palette.canvas,
            onBackground = ColorTokens.darkOnBackground,
            surface = palette.surfaceElevated,
            onSurface = ColorTokens.darkOnBackground,
            surfaceVariant = palette.surfaceSoft,
            onSurfaceVariant = palette.onSurfaceMuted,
            outline = ColorTokens.darkOutline,
        )

        else -> lightColorScheme(
            primary = palette.brand,
            onPrimary = ColorTokens.onBrand,
            primaryContainer = palette.brandContainer,
            onPrimaryContainer = ColorTokens.onBrandContainer,
            secondary = palette.brandStrong,
            onSecondary = ColorTokens.onBrand,
            background = palette.canvas,
            onBackground = ColorTokens.onBackground,
            surface = palette.surfaceElevated,
            onSurface = ColorTokens.onBackground,
            surfaceVariant = palette.surfaceSoft,
            onSurfaceVariant = palette.onSurfaceMuted,
            outline = ColorTokens.outline,
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

private object ColorTokens {
    val onBrand = androidx.compose.ui.graphics.Color.White
    val onBrandContainer = androidx.compose.ui.graphics.Color(0xFF172B70)
    val onBackground = androidx.compose.ui.graphics.Color(0xFF171A22)
    val outline = androidx.compose.ui.graphics.Color(0xFFDDE2EC)
    val darkOnBrand = androidx.compose.ui.graphics.Color(0xFF172B70)
    val darkOnBrandContainer = androidx.compose.ui.graphics.Color(0xFFDCE2FF)
    val darkOnBackground = androidx.compose.ui.graphics.Color(0xFFE5E6EE)
    val darkOutline = androidx.compose.ui.graphics.Color(0xFF454852)
}

val ColorScheme.cardBorder: androidx.compose.ui.graphics.Color
    get() = outlineVariant

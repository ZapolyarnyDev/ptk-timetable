package io.github.zapolyarnydev.ptktimetable.data.preferences

import androidx.compose.ui.graphics.Color
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppThemeMode
import io.github.zapolyarnydev.ptktimetable.ui.theme.ThemeManager
import io.github.zapolyarnydev.ptktimetable.ui.theme.toThemeSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppearancePreferencesTest {

    @Test
    fun invalidValuesFallBackToSafeDefaults() {
        val result = AppearancePreferences(
            primaryTextColorArgb = -1,
            secondaryTextColorArgb = AppearancePreferences.MAX_ARGB + 1,
            backgroundColorArgb = 0xFF112233,
            secondarySurfaceOpacity = Float.NaN,
        ).sanitized()

        assertNull(result.primaryTextColorArgb)
        assertNull(result.secondaryTextColorArgb)
        assertEquals(0xFF112233, result.backgroundColorArgb)
        assertEquals(AppearancePreferences.DEFAULT_SECONDARY_SURFACE_OPACITY, result.secondarySurfaceOpacity)
    }

    @Test
    fun themeModeUsesStableStorageValuesAndUnknownValueIsSystem() {
        assertEquals("system", AppThemeMode.SYSTEM.storageValue)
        assertEquals("light", AppThemeMode.LIGHT.storageValue)
        assertEquals("dark", AppThemeMode.DARK.storageValue)
        assertEquals(AppThemeMode.LIGHT, AppThemeMode.fromStorageValue("amoled"))
        assertEquals(AppThemeMode.LIGHT, AppThemeMode.fromStorageValue(null))
    }

    @Test
    fun hexParserAcceptsRgbAndArgbWithoutThrowingOnBadInput() {
        assertEquals(0xFF315FEA, parseArgbHex("#315FEA"))
        assertEquals(0x80315FEA, parseArgbHex("80315fea"))
        assertEquals("#80315FEA", formatArgbHex(0x80315FEA))
        assertNull(parseArgbHex("#12345"))
        assertNull(parseArgbHex("not-a-color"))
    }

    @Test
    fun storedArgbUsesTheComposeArgbColorRepresentation() {
        val settings = AppearancePreferences(accentColorArgb = 0xFF315FEA).toThemeSettings()

        assertEquals(Color(0xFF315FEA), ThemeManager.palette(settings, dark = false).accent)
    }
}

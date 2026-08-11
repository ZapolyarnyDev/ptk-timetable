package io.github.zapolyarnydev.ptktimetable.data.preferences

import io.github.zapolyarnydev.ptktimetable.ui.theme.AppThemeMode

data class AppearancePreferences(
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val primaryTextColorArgb: Long? = null,
    val secondaryTextColorArgb: Long? = null,
    val backgroundColorArgb: Long? = null,
    val accentColorArgb: Long? = null,
    val secondarySurfaceOpacity: Float = DEFAULT_SECONDARY_SURFACE_OPACITY,
) {
    fun sanitized(): AppearancePreferences = copy(
        primaryTextColorArgb = primaryTextColorArgb.validArgbOrNull(),
        secondaryTextColorArgb = secondaryTextColorArgb.validArgbOrNull(),
        backgroundColorArgb = backgroundColorArgb.validArgbOrNull(),
        accentColorArgb = accentColorArgb.validArgbOrNull(),
        secondarySurfaceOpacity = secondarySurfaceOpacity
            .takeIf { it.isFinite() && it in 0f..1f }
            ?: DEFAULT_SECONDARY_SURFACE_OPACITY,
    )

    companion object {
        const val DEFAULT_SECONDARY_SURFACE_OPACITY = 1f
        const val MAX_ARGB = 0xFFFF_FFFFL

        val Defaults = AppearancePreferences()
    }
}

internal fun Long?.validArgbOrNull(): Long? = this?.takeIf { it in 0L..AppearancePreferences.MAX_ARGB }

fun parseArgbHex(input: String): Long? {
    val digits = input.trim().removePrefix("#")
    val normalized = when (digits.length) {
        6 -> "FF$digits"
        8 -> digits
        else -> return null
    }
    return normalized.toLongOrNull(16)?.validArgbOrNull()
}

fun formatArgbHex(argb: Long): String = "#%08X".format(argb and AppearancePreferences.MAX_ARGB)

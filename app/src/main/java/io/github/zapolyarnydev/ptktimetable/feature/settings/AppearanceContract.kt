package io.github.zapolyarnydev.ptktimetable.feature.settings

import io.github.zapolyarnydev.ptktimetable.data.preferences.AppearancePreferences
import io.github.zapolyarnydev.ptktimetable.data.preferences.formatArgbHex
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppThemeMode

enum class AppearanceColorTarget {
    PRIMARY_TEXT,
    SECONDARY_TEXT,
    BACKGROUND,
    ACCENT,
}

data class AppearanceColorInput(val text: String = "", val isValid: Boolean = true)

data class AppearanceUiState(
    val isLoading: Boolean = true,
    val saved: AppearancePreferences = AppearancePreferences.Defaults,
    val draft: AppearancePreferences = AppearancePreferences.Defaults,
    val colorInputs: Map<AppearanceColorTarget, AppearanceColorInput> = emptyMap(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    val hasChanges: Boolean get() = draft != saved
    val canSave: Boolean get() = !isLoading && !isSaving && hasChanges && colorInputs.values.all { it.isValid }
}

sealed interface AppearanceUiAction {
    data class SelectThemeMode(val mode: AppThemeMode) : AppearanceUiAction

    data class UpdateColorHex(val target: AppearanceColorTarget, val value: String) : AppearanceUiAction

    data class SelectPresetColor(val target: AppearanceColorTarget, val argb: Long) : AppearanceUiAction

    data class ResetColor(val target: AppearanceColorTarget) : AppearanceUiAction

    data object ResetDefaults : AppearanceUiAction

    data object Save : AppearanceUiAction
}

internal fun AppearancePreferences.toColorInputs(): Map<AppearanceColorTarget, AppearanceColorInput> = mapOf(
    AppearanceColorTarget.PRIMARY_TEXT to primaryTextColorArgb.toColorInput(),
    AppearanceColorTarget.SECONDARY_TEXT to secondaryTextColorArgb.toColorInput(),
    AppearanceColorTarget.BACKGROUND to backgroundColorArgb.toColorInput(),
    AppearanceColorTarget.ACCENT to accentColorArgb.toColorInput(),
)

private fun Long?.toColorInput(): AppearanceColorInput =
    AppearanceColorInput(text = this?.let(::formatArgbHex).orEmpty())

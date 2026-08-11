package io.github.zapolyarnydev.ptktimetable.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.zapolyarnydev.ptktimetable.data.preferences.AppearancePreferences
import io.github.zapolyarnydev.ptktimetable.data.preferences.AppearancePreferencesRepository
import io.github.zapolyarnydev.ptktimetable.data.preferences.formatArgbHex
import io.github.zapolyarnydev.ptktimetable.data.preferences.parseArgbHex
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppearanceViewModel(private val repository: AppearancePreferencesRepository) : ViewModel() {

    private val _state = MutableStateFlow(AppearanceUiState())
    val state: StateFlow<AppearanceUiState> = _state.asStateFlow()

    private var saveJob: Job? = null

    init {
        load()
    }

    fun onAction(action: AppearanceUiAction) {
        when (action) {
            is AppearanceUiAction.SelectThemeMode -> updateDraft { copy(themeMode = action.mode) }

            is AppearanceUiAction.UpdateColorHex -> updateColorHex(action.target, action.value)

            is AppearanceUiAction.SelectPresetColor -> setColor(action.target, action.argb)

            is AppearanceUiAction.ResetColor -> setColor(action.target, null)

            is AppearanceUiAction.SetSecondarySurfaceOpacity -> updateDraft {
                copy(secondarySurfaceOpacity = action.opacity.coerceIn(0f, 1f))
            }

            AppearanceUiAction.ResetDefaults -> _state.update {
                it.copy(
                    draft = AppearancePreferences.Defaults,
                    colorInputs = AppearancePreferences.Defaults.toColorInputs(),
                    errorMessage = null,
                )
            }

            AppearanceUiAction.Save -> save()
        }
    }

    private fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val preferences = repository.preferences.first()
                _state.value = AppearanceUiState(
                    isLoading = false,
                    saved = preferences,
                    draft = preferences,
                    colorInputs = preferences.toColorInputs(),
                )
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Не удалось загрузить настройки внешнего вида",
                    )
                }
            }
        }
    }

    private fun updateColorHex(target: AppearanceColorTarget, rawValue: String) {
        val value = rawValue.trim().filterIndexed { index, char ->
            (index == 0 && char == '#') || char.isDigit() || char.lowercaseChar() in 'a'..'f'
        }.take(9)
        val parsed = parseArgbHex(value)
        _state.update { current ->
            current.copy(
                draft = parsed?.let { current.draft.withColor(target, it) } ?: current.draft,
                colorInputs = current.colorInputs + (target to AppearanceColorInput(value, parsed != null)),
                errorMessage = null,
            )
        }
    }

    private fun setColor(target: AppearanceColorTarget, argb: Long?) {
        _state.update { current ->
            current.copy(
                draft = current.draft.withColor(target, argb),
                colorInputs = current.colorInputs + (
                    target to AppearanceColorInput(argb?.let(::formatArgbHex).orEmpty())
                    ),
                errorMessage = null,
            )
        }
    }

    private fun updateDraft(transform: AppearancePreferences.() -> AppearancePreferences) {
        _state.update { it.copy(draft = it.draft.transform(), errorMessage = null) }
    }

    private fun save() {
        val snapshot = _state.value
        if (!snapshot.canSave) return
        saveJob?.cancel()
        saveJob = viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val safe = snapshot.draft.sanitized()
                repository.save(safe)
                _state.update { current ->
                    val draftWasUnchanged = current.draft == snapshot.draft
                    current.copy(
                        saved = safe,
                        draft = if (draftWasUnchanged) safe else current.draft,
                        colorInputs = if (draftWasUnchanged) safe.toColorInputs() else current.colorInputs,
                        isSaving = false,
                    )
                }
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                _state.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Не удалось сохранить настройки внешнего вида",
                    )
                }
            }
        }
    }
}

private fun AppearancePreferences.withColor(target: AppearanceColorTarget, argb: Long?): AppearancePreferences =
    when (target) {
        AppearanceColorTarget.PRIMARY_TEXT -> copy(primaryTextColorArgb = argb)
        AppearanceColorTarget.SECONDARY_TEXT -> copy(secondaryTextColorArgb = argb)
        AppearanceColorTarget.BACKGROUND -> copy(backgroundColorArgb = argb)
        AppearanceColorTarget.ACCENT -> copy(accentColorArgb = argb)
    }

class AppearanceViewModelFactory(private val repository: AppearancePreferencesRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppearanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppearanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

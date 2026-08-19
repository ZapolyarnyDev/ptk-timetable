package io.github.zapolyarnydev.ptktimetable.feature.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zapolyarnydev.ptktimetable.data.preferences.AppearancePreferences
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppDimensions
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppShapes
import io.github.zapolyarnydev.ptktimetable.ui.theme.MaterialThemeAppColors
import io.github.zapolyarnydev.ptktimetable.ui.theme.ThemeManager
import io.github.zapolyarnydev.ptktimetable.ui.theme.toThemeSettings

@Composable
fun AppearanceRoute(
    viewModel: AppearanceViewModel,
    onBack: () -> Unit,
    onAppearancePreview: (AppearancePreferences?) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentPreviewCallback by rememberUpdatedState(onAppearancePreview)
    DisposableEffect(Unit) {
        onDispose { currentPreviewCallback(null) }
    }
    SideEffect {
        if (!state.isLoading) currentPreviewCallback(state.draft)
    }
    AppearanceScreen(state = state, onAction = viewModel::onAction, onBack = onBack)
}

@Composable
fun AppearanceScreen(state: AppearanceUiState, onAction: (AppearanceUiAction) -> Unit, onBack: () -> Unit) {
    Scaffold(containerColor = MaterialThemeAppColors.background) { scaffoldPadding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(scaffoldPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val themeSettings = state.draft.toThemeSettings()
        val palette = ThemeManager.palette(
            themeSettings,
            ThemeManager.isDark(themeSettings, isSystemInDarkTheme()),
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(scaffoldPadding).testTag("appearance-settings"),
            contentPadding = PaddingValues(
                start = AppDimensions.screenHorizontalPadding,
                top = AppDimensions.screenVerticalPadding,
                end = AppDimensions.screenHorizontalPadding,
                bottom = 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.sectionSpacing),
        ) {
            item { AppearanceTopBar(onBack) }
            item {
                ThemeModeSelector(state.draft.themeMode) {
                    onAction(AppearanceUiAction.SelectThemeMode(it))
                }
            }
            item { AppearancePreview(state.draft) }
            item {
                AppearanceSection("Цвета", "Готовые варианты или ручной ARGB/HEX") {
                    AppearanceColorEditor(
                        title = "Основной текст",
                        value = state.colorInput(AppearanceColorTarget.PRIMARY_TEXT),
                        resolvedColor = palette.textPrimary,
                        presets = colorPresetsFor(AppearanceColorTarget.PRIMARY_TEXT),
                        onHexChange = {
                            onAction(AppearanceUiAction.UpdateColorHex(AppearanceColorTarget.PRIMARY_TEXT, it))
                        },
                        onPreset = {
                            onAction(AppearanceUiAction.SelectPresetColor(AppearanceColorTarget.PRIMARY_TEXT, it))
                        },
                        onReset = { onAction(AppearanceUiAction.ResetColor(AppearanceColorTarget.PRIMARY_TEXT)) },
                    )
                    Spacer(Modifier.height(18.dp))
                    AppearanceColorEditor(
                        title = "Вторичный текст",
                        value = state.colorInput(AppearanceColorTarget.SECONDARY_TEXT),
                        resolvedColor = palette.textSecondary,
                        presets = colorPresetsFor(AppearanceColorTarget.SECONDARY_TEXT),
                        onHexChange = {
                            onAction(AppearanceUiAction.UpdateColorHex(AppearanceColorTarget.SECONDARY_TEXT, it))
                        },
                        onPreset = {
                            onAction(AppearanceUiAction.SelectPresetColor(AppearanceColorTarget.SECONDARY_TEXT, it))
                        },
                        onReset = { onAction(AppearanceUiAction.ResetColor(AppearanceColorTarget.SECONDARY_TEXT)) },
                    )
                    Spacer(Modifier.height(18.dp))
                    AppearanceColorEditor(
                        title = "Фон",
                        value = state.colorInput(AppearanceColorTarget.BACKGROUND),
                        resolvedColor = palette.background,
                        presets = colorPresetsFor(AppearanceColorTarget.BACKGROUND),
                        onHexChange = {
                            onAction(AppearanceUiAction.UpdateColorHex(AppearanceColorTarget.BACKGROUND, it))
                        },
                        onPreset = {
                            onAction(AppearanceUiAction.SelectPresetColor(AppearanceColorTarget.BACKGROUND, it))
                        },
                        onReset = { onAction(AppearanceUiAction.ResetColor(AppearanceColorTarget.BACKGROUND)) },
                    )
                    Spacer(Modifier.height(18.dp))
                    AppearanceColorEditor(
                        title = "Акцент",
                        value = state.colorInput(AppearanceColorTarget.ACCENT),
                        resolvedColor = palette.accent,
                        presets = colorPresetsFor(AppearanceColorTarget.ACCENT),
                        onHexChange = { onAction(AppearanceUiAction.UpdateColorHex(AppearanceColorTarget.ACCENT, it)) },
                        onPreset = { onAction(AppearanceUiAction.SelectPresetColor(AppearanceColorTarget.ACCENT, it)) },
                        onReset = { onAction(AppearanceUiAction.ResetColor(AppearanceColorTarget.ACCENT)) },
                    )
                }
            }
            state.errorMessage?.let { message ->
                item {
                    Text(message, color = MaterialThemeAppColors.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
            item {
                Button(
                    onClick = { onAction(AppearanceUiAction.Save) },
                    enabled = state.canSave,
                    modifier = Modifier.fillMaxWidth().testTag("save-appearance"),
                    shape = AppShapes.pill,
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.padding(2.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (state.hasChanges) "Сохранить изменения" else "Настройки сохранены")
                    }
                }
                TextButton(
                    onClick = { onAction(AppearanceUiAction.ResetDefaults) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Восстановить стандартную тему") }
            }
        }
    }
}

private fun AppearanceUiState.colorInput(target: AppearanceColorTarget): AppearanceColorInput =
    colorInputs[target] ?: AppearanceColorInput()

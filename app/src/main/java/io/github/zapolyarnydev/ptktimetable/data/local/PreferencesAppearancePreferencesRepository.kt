package io.github.zapolyarnydev.ptktimetable.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.zapolyarnydev.ptktimetable.data.preferences.AppearancePreferences
import io.github.zapolyarnydev.ptktimetable.data.preferences.AppearancePreferencesRepository
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class PreferencesAppearancePreferencesRepository(private val dataStore: DataStore<Preferences>) :
    AppearancePreferencesRepository {

    override val preferences: Flow<AppearancePreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(
                    androidx.datastore.preferences.core.emptyPreferences(),
                )
            } else {
                throw exception
            }
        }
        .map(::decode)

    override suspend fun save(preferences: AppearancePreferences) {
        val safe = preferences.sanitized()
        dataStore.edit { values ->
            values[AppearancePreferenceKeys.THEME_MODE] = safe.themeMode.storageValue
            values.writeColor(
                enabledKey = AppearancePreferenceKeys.USE_PRIMARY_TEXT_COLOR,
                colorKey = AppearancePreferenceKeys.PRIMARY_TEXT_COLOR,
                argb = safe.primaryTextColorArgb,
            )
            values.writeColor(
                enabledKey = AppearancePreferenceKeys.USE_SECONDARY_TEXT_COLOR,
                colorKey = AppearancePreferenceKeys.SECONDARY_TEXT_COLOR,
                argb = safe.secondaryTextColorArgb,
            )
            values.writeColor(
                enabledKey = AppearancePreferenceKeys.USE_BACKGROUND_COLOR,
                colorKey = AppearancePreferenceKeys.BACKGROUND_COLOR,
                argb = safe.backgroundColorArgb,
            )
            values.writeColor(
                enabledKey = AppearancePreferenceKeys.USE_ACCENT_COLOR,
                colorKey = AppearancePreferenceKeys.ACCENT_COLOR,
                argb = safe.accentColorArgb,
            )
        }
    }

    override suspend fun reset() {
        dataStore.edit { it.clear() }
    }

    private fun decode(values: Preferences): AppearancePreferences = AppearancePreferences(
        themeMode = AppThemeMode.fromStorageValue(values[AppearancePreferenceKeys.THEME_MODE]),
        primaryTextColorArgb = values.readColor(
            AppearancePreferenceKeys.USE_PRIMARY_TEXT_COLOR,
            AppearancePreferenceKeys.PRIMARY_TEXT_COLOR,
        ),
        secondaryTextColorArgb = values.readColor(
            AppearancePreferenceKeys.USE_SECONDARY_TEXT_COLOR,
            AppearancePreferenceKeys.SECONDARY_TEXT_COLOR,
        ),
        backgroundColorArgb = values.readColor(
            AppearancePreferenceKeys.USE_BACKGROUND_COLOR,
            AppearancePreferenceKeys.BACKGROUND_COLOR,
        ),
        accentColorArgb = values.readColor(
            AppearancePreferenceKeys.USE_ACCENT_COLOR,
            AppearancePreferenceKeys.ACCENT_COLOR,
        ),
    ).sanitized()
}

private fun Preferences.readColor(enabledKey: Preferences.Key<Boolean>, colorKey: Preferences.Key<Long>): Long? =
    this[colorKey].takeIf { this[enabledKey] == true }

private fun androidx.datastore.preferences.core.MutablePreferences.writeColor(
    enabledKey: Preferences.Key<Boolean>,
    colorKey: Preferences.Key<Long>,
    argb: Long?,
) {
    this[enabledKey] = argb != null
    if (argb == null) remove(colorKey) else this[colorKey] = argb
}

internal object AppearancePreferenceKeys {
    val THEME_MODE = stringPreferencesKey("appearance_theme_mode")
    val USE_PRIMARY_TEXT_COLOR = booleanPreferencesKey("appearance_use_primary_text_color")
    val PRIMARY_TEXT_COLOR = longPreferencesKey("appearance_primary_text_color_argb")
    val USE_SECONDARY_TEXT_COLOR = booleanPreferencesKey("appearance_use_secondary_text_color")
    val SECONDARY_TEXT_COLOR = longPreferencesKey("appearance_secondary_text_color_argb")
    val USE_BACKGROUND_COLOR = booleanPreferencesKey("appearance_use_background_color")
    val BACKGROUND_COLOR = longPreferencesKey("appearance_background_color_argb")
    val USE_ACCENT_COLOR = booleanPreferencesKey("appearance_use_accent_color")
    val ACCENT_COLOR = longPreferencesKey("appearance_accent_color_argb")
}

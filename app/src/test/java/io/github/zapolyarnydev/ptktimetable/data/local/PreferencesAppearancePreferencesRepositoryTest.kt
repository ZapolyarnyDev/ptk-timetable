package io.github.zapolyarnydev.ptktimetable.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import io.github.zapolyarnydev.ptktimetable.data.preferences.AppearancePreferences
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.nio.file.Files

class PreferencesAppearancePreferencesRepositoryTest {

    @Test
    fun savedPreferencesAreAvailableToANewRepositoryInstance() = withDataStore { dataStore ->
        val expected = AppearancePreferences(
            themeMode = AppThemeMode.DARK,
            primaryTextColorArgb = 0xFFEDEDED,
            secondaryTextColorArgb = 0xFFB0B0B0,
            backgroundColorArgb = 0xFF101114,
            accentColorArgb = 0xFF7C9BFF,
            secondarySurfaceOpacity = 0.4f,
        )

        PreferencesAppearancePreferencesRepository(dataStore).save(expected)

        val restored = PreferencesAppearancePreferencesRepository(dataStore).preferences.first()
        assertEquals(expected, restored)
    }

    @Test
    fun malformedStoredValuesAreReplacedWithDefaults() = withDataStore { dataStore ->
        dataStore.edit { values ->
            values[AppearancePreferenceKeys.THEME_MODE] = "legacy-amoled"
            values[AppearancePreferenceKeys.USE_PRIMARY_TEXT_COLOR] = true
            values[AppearancePreferenceKeys.PRIMARY_TEXT_COLOR] = -42
            values[AppearancePreferenceKeys.USE_ACCENT_COLOR] = true
            values[AppearancePreferenceKeys.ACCENT_COLOR] = AppearancePreferences.MAX_ARGB + 1
            values[AppearancePreferenceKeys.SECONDARY_SURFACE_OPACITY] = 4.2f
        }

        val restored = PreferencesAppearancePreferencesRepository(dataStore).preferences.first()
        assertEquals(AppThemeMode.LIGHT, restored.themeMode)
        assertNull(restored.primaryTextColorArgb)
        assertNull(restored.accentColorArgb)
        assertEquals(AppearancePreferences.DEFAULT_SECONDARY_SURFACE_OPACITY, restored.secondarySurfaceOpacity)
    }

    private fun withDataStore(block: suspend (DataStore<Preferences>) -> Unit) = runBlocking {
        val directory = Files.createTempDirectory("appearance-preferences-test").toFile()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val dataStore = PreferenceDataStoreFactory.create(scope = scope) {
                directory.resolve("preferences.preferences_pb")
            }
            block(dataStore)
        } finally {
            scope.cancel()
            directory.deleteRecursively()
        }
    }
}

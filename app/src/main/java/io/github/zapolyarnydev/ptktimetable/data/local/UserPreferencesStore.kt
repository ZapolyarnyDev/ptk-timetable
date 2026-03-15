package io.github.zapolyarnydev.ptktimetable.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException

private const val USER_SETTINGS_DATASTORE = "user_settings"
private val Context.userPreferencesDataStore by preferencesDataStore(name = USER_SETTINGS_DATASTORE)

class UserPreferencesStore(
    private val context: Context
) {

    suspend fun getLastSelectedGroupName(): String? {
        return context.userPreferencesDataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .first()[LAST_SELECTED_GROUP_KEY]
    }

    suspend fun setLastSelectedGroupName(groupName: String?) {
        context.userPreferencesDataStore.edit { preferences ->
            if (groupName.isNullOrBlank()) {
                preferences.remove(LAST_SELECTED_GROUP_KEY)
            } else {
                preferences[LAST_SELECTED_GROUP_KEY] = groupName.trim()
            }
        }
    }

    private companion object {
        val LAST_SELECTED_GROUP_KEY: Preferences.Key<String> =
            stringPreferencesKey("last_selected_group")
    }
}

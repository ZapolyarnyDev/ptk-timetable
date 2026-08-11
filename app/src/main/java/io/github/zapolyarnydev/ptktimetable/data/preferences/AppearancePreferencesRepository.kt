package io.github.zapolyarnydev.ptktimetable.data.preferences

import kotlinx.coroutines.flow.Flow

interface AppearancePreferencesRepository {
    val preferences: Flow<AppearancePreferences>

    suspend fun save(preferences: AppearancePreferences)
}

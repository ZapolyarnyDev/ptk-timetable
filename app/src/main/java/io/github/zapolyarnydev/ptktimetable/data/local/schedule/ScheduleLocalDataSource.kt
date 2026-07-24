package io.github.zapolyarnydev.ptktimetable.data.local.schedule

import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.CachedData
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Group
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Lesson
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface ScheduleLocalDataSource {
    fun observeGroups(): Flow<CachedData<List<Group>>>

    fun observeLessons(groupName: String): Flow<CachedData<List<Lesson>>>

    suspend fun saveGroups(groups: List<Group>, syncedAt: Instant)

    suspend fun replaceLessons(groupName: String, lessons: List<Lesson>, syncedAt: Instant)
}

package io.github.zapolyarnydev.ptktimetable.data.local.schedule

import androidx.room.withTransaction
import io.github.zapolyarnydev.ptktimetable.data.local.database.AppDatabase
import io.github.zapolyarnydev.ptktimetable.data.local.database.entity.SyncMetadataEntity
import io.github.zapolyarnydev.ptktimetable.data.mapper.RoomScheduleMapper
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.CachedData
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Group
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Lesson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.util.Locale

class RoomScheduleLocalDataSource(private val database: AppDatabase, private val mapper: RoomScheduleMapper) :
    ScheduleLocalDataSource {

    override fun observeGroups(): Flow<CachedData<List<Group>>> = combine(
        database.groupDao().observeAll(),
        database.syncMetadataDao().observe(GROUPS_RESOURCE),
    ) { groups, metadata ->
        CachedData(
            data = groups.map(mapper::toDomain),
            updatedAt = metadata?.lastSuccessfulSyncEpochMillis?.let(Instant::ofEpochMilli),
        )
    }

    override fun observeLessons(groupName: String): Flow<CachedData<List<Lesson>>> = combine(
        database.lessonTemplateDao().observeByGroup(groupName),
        database.syncMetadataDao().observe(lessonsResource(groupName)),
    ) { lessons, metadata ->
        CachedData(
            data = lessons.mapNotNull(mapper::toDomain),
            updatedAt = metadata?.lastSuccessfulSyncEpochMillis?.let(Instant::ofEpochMilli),
        )
    }

    override suspend fun saveGroups(groups: List<Group>, syncedAt: Instant) {
        database.withTransaction {
            database.groupDao().saveAll(groups.map(mapper::toEntity))
            database.syncMetadataDao().upsert(
                SyncMetadataEntity(
                    resourceKey = GROUPS_RESOURCE,
                    lastSuccessfulSyncEpochMillis = syncedAt.toEpochMilli(),
                ),
            )
        }
    }

    override suspend fun replaceLessons(groupName: String, lessons: List<Lesson>, syncedAt: Instant) {
        database.withTransaction {
            database.lessonTemplateDao().replaceForGroup(
                groupName = groupName,
                lessons = lessons.map(mapper::toEntity),
            )
            database.syncMetadataDao().upsert(
                SyncMetadataEntity(
                    resourceKey = lessonsResource(groupName),
                    lastSuccessfulSyncEpochMillis = syncedAt.toEpochMilli(),
                ),
            )
        }
    }

    private companion object {
        const val GROUPS_RESOURCE = "groups"

        fun lessonsResource(groupName: String): String = "lessons:${groupName.trim().lowercase(Locale.ROOT)}"
    }
}

package io.github.zapolyarnydev.ptktimetable.data.repository

import io.github.zapolyarnydev.ptktimetable.data.local.schedule.ScheduleLocalDataSource
import io.github.zapolyarnydev.ptktimetable.data.mapper.NovsuLessonMapper
import io.github.zapolyarnydev.ptktimetable.data.remote.NovsuScheduleDataSource
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.CachedData
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Group
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Lesson
import io.github.zapolyarnydev.ptktimetable.domain.schedule.repository.TimetableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.Instant
import java.util.Locale

class DefaultTimetableRepository(
    private val remoteDataSource: NovsuScheduleDataSource,
    private val localDataSource: ScheduleLocalDataSource,
    private val lessonMapper: NovsuLessonMapper,
    private val clock: Clock = Clock.systemUTC(),
) : TimetableRepository {

    private val requestMutex = Mutex()
    private val requestVersions = mutableMapOf<String, Long>()

    override fun observeGroups(): Flow<CachedData<List<Group>>> = localDataSource.observeGroups()

    override fun observeLessons(groupName: String): Flow<CachedData<List<Lesson>>> =
        localDataSource.observeLessons(groupName)

    override suspend fun refreshGroups() {
        val requestVersion = beginRequest(GROUPS_REQUEST)
        val groups = remoteDataSource.getGroups()
        saveIfLatest(GROUPS_REQUEST, requestVersion) {
            localDataSource.saveGroups(groups, Instant.now(clock))
        }
    }

    override suspend fun refreshLessons(group: Group) {
        val requestKey = lessonsRequest(group.groupName)
        val requestVersion = beginRequest(requestKey)
        val lessons = lessonMapper.map(
            remoteDataSource.getScheduleForGroup(group.groupName, group.sourceUrl),
        )
        saveIfLatest(requestKey, requestVersion) {
            localDataSource.replaceLessons(group.groupName, lessons, Instant.now(clock))
        }
    }

    private suspend fun beginRequest(key: String): Long = requestMutex.withLock {
        val nextVersion = requestVersions.getOrDefault(key, 0L) + 1L
        requestVersions[key] = nextVersion
        nextVersion
    }

    private suspend fun saveIfLatest(key: String, version: Long, save: suspend () -> Unit) {
        requestMutex.lock()
        try {
            if (requestVersions[key] == version) save()
        } finally {
            requestMutex.unlock()
        }
    }

    private companion object {
        const val GROUPS_REQUEST = "groups"

        fun lessonsRequest(groupName: String): String = "lessons:${groupName.trim().lowercase(Locale.ROOT)}"
    }
}

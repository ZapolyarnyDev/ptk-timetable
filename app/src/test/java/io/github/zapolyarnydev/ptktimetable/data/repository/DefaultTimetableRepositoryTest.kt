package io.github.zapolyarnydev.ptktimetable.data.repository

import io.github.zapolyarnydev.ptktimetable.data.local.schedule.ScheduleLocalDataSource
import io.github.zapolyarnydev.ptktimetable.data.mapper.NovsuLessonMapper
import io.github.zapolyarnydev.ptktimetable.data.remote.NovsuScheduleDataSource
import io.github.zapolyarnydev.ptktimetable.data.remote.xls.NovsuRawLesson
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.CachedData
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Group
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Lesson
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicInteger

class DefaultTimetableRepositoryTest {

    private val fixedClock = Clock.fixed(Instant.parse("2026-04-26T00:00:00Z"), ZoneOffset.UTC)
    private val group = Group("College", 1, "Course", "ISP-1", "schedule.xls")

    @Test
    fun `refreshLessons remaps saturday portal slots and stores them locally`() = runBlocking {
        val local = FakeLocalDataSource()
        val repository = repository(
            remote = FakeScheduleDataSource {
                listOf(
                    rawLesson(dayOfWeek = "сб", timeRange = "8.30-10.10", rawText = "Математика"),
                    rawLesson(dayOfWeek = "сб", timeRange = "10.20-12.00", rawText = "Физика"),
                    rawLesson(dayOfWeek = "пн", timeRange = "8.30-10.10", rawText = "История"),
                )
            },
            local = local,
        )

        repository.refreshLessons(group)
        val templates = local.lessonFlows.getValue(group.groupName).value.data

        assertEquals("08:30", templates[0].startTime.toString())
        assertEquals("10:10", templates[0].endTime.toString())
        assertEquals("08:30", templates[1].startTime.toString())
        assertEquals("09:30", templates[1].endTime.toString())
        assertEquals("09:40", templates[2].startTime.toString())
        assertEquals("10:40", templates[2].endTime.toString())
    }

    @Test
    fun `cached lessons are available without waiting for network`() = runBlocking {
        val cachedLesson = lesson("cached")
        val local = FakeLocalDataSource(
            initialLessons = mapOf(group.groupName to listOf(cachedLesson)),
        )
        val repository = repository(
            remote = FakeScheduleDataSource { error("Network should not be called") },
            local = local,
        )

        val cached = repository.observeLessons(group.groupName).first()

        assertEquals(listOf(cachedLesson), cached.data)
    }

    @Test
    fun `groups refresh updates the local flow`() = runBlocking {
        val local = FakeLocalDataSource()
        val repository = repository(
            remote = FakeScheduleDataSource(
                groups = listOf(group),
                lessons = { emptyList() },
            ),
            local = local,
        )

        repository.refreshGroups()

        assertEquals(listOf(group), repository.observeGroups().first().data)
    }

    @Test
    fun `failed refresh keeps cached lessons`() = runBlocking {
        val cachedLesson = lesson("cached")
        val local = FakeLocalDataSource(
            initialLessons = mapOf(group.groupName to listOf(cachedLesson)),
        )
        val repository = repository(
            remote = FakeScheduleDataSource { error("XLS broke") },
            local = local,
        )

        val result = runCatching { repository.refreshLessons(group) }

        assertTrue(result.isFailure)
        assertEquals(listOf(cachedLesson), repository.observeLessons(group.groupName).first().data)
    }

    @Test
    fun `older response cannot overwrite a newer refresh`() = runBlocking {
        val firstStarted = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        val callCount = AtomicInteger()
        val local = FakeLocalDataSource()
        val repository = repository(
            remote = FakeScheduleDataSource {
                if (callCount.incrementAndGet() == 1) {
                    firstStarted.complete(Unit)
                    releaseFirst.await()
                    listOf(rawLesson(rawText = "Old"))
                } else {
                    listOf(rawLesson(rawText = "New"))
                }
            },
            local = local,
        )

        val olderRefresh = async { repository.refreshLessons(group) }
        firstStarted.await()
        repository.refreshLessons(group)
        releaseFirst.complete(Unit)
        olderRefresh.await()

        val storedSubjects = repository.observeLessons(group.groupName).first().data.map { it.subject }
        assertEquals(listOf("New"), storedSubjects)
    }

    private fun repository(remote: NovsuScheduleDataSource, local: ScheduleLocalDataSource) =
        DefaultTimetableRepository(
            remoteDataSource = remote,
            localDataSource = local,
            lessonMapper = NovsuLessonMapper(clock = fixedClock),
            clock = fixedClock,
        )

    private fun rawLesson(dayOfWeek: String = "пн", timeRange: String = "8.30-10.10", rawText: String) = NovsuRawLesson(
        groupName = group.groupName,
        dayOfWeek = dayOfWeek,
        timeRange = timeRange,
        rawText = rawText,
        weekType = WeekType.ALL,
    )

    private fun lesson(subject: String) = Lesson(
        id = subject,
        groupName = group.groupName,
        dayOfWeek = java.time.DayOfWeek.MONDAY,
        startTime = java.time.LocalTime.of(8, 30),
        endTime = java.time.LocalTime.of(10, 10),
        weekType = WeekType.ALL,
        subject = subject,
        teacher = null,
        room = null,
        rawText = subject,
        sourceUpdatedAt = Instant.now(fixedClock),
    )

    private class FakeScheduleDataSource(
        private val groups: List<Group> = emptyList(),
        private val lessons: suspend () -> List<NovsuRawLesson>,
    ) : NovsuScheduleDataSource {

        override suspend fun getGroups(): List<Group> = groups

        override suspend fun getScheduleForGroup(groupName: String, xlsUrl: String): List<NovsuRawLesson> = lessons()

        override suspend fun getCurrentWeekType(): WeekType? = null

        override suspend fun getWeekTypeForDate(date: LocalDate): WeekType? = null
    }

    private class FakeLocalDataSource(initialLessons: Map<String, List<Lesson>> = emptyMap()) :
        ScheduleLocalDataSource {

        val groupsFlow = MutableStateFlow(CachedData<List<Group>>(emptyList(), null))
        val lessonFlows = initialLessons
            .mapValuesTo(mutableMapOf()) { (_, lessons) ->
                MutableStateFlow(CachedData(lessons, Instant.EPOCH))
            }

        override fun observeGroups(): Flow<CachedData<List<Group>>> = groupsFlow

        override fun observeLessons(groupName: String): Flow<CachedData<List<Lesson>>> =
            lessonFlows.getOrPut(groupName) {
                MutableStateFlow(CachedData(emptyList(), null))
            }

        override suspend fun saveGroups(groups: List<Group>, syncedAt: Instant) {
            groupsFlow.value = CachedData(groups, syncedAt)
        }

        override suspend fun replaceLessons(groupName: String, lessons: List<Lesson>, syncedAt: Instant) {
            lessonFlows.getOrPut(groupName) {
                MutableStateFlow(CachedData(emptyList(), null))
            }.value = CachedData(lessons, syncedAt)
        }
    }
}

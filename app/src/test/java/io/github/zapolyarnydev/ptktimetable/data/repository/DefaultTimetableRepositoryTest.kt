package io.github.zapolyarnydev.ptktimetable.data.repository

import io.github.zapolyarnydev.ptktimetable.data.mapper.NovsuLessonMapper
import io.github.zapolyarnydev.ptktimetable.data.remote.NovsuScheduleDataSource
import io.github.zapolyarnydev.ptktimetable.data.remote.xls.NovsuRawLesson
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Group
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class DefaultTimetableRepositoryTest {

    @Test
    fun `getLessons remaps saturday portal slots to local saturday bell schedule`() = runBlocking {
        val repository = DefaultTimetableRepository(
            remoteDataSource = FakeScheduleDataSource(
                lessons = listOf(
                    rawLesson(dayOfWeek = "сб", timeRange = "8.30-10.10", rawText = "Математика"),
                    rawLesson(dayOfWeek = "сб", timeRange = "10.20-12.00", rawText = "Физика"),
                    rawLesson(dayOfWeek = "пн", timeRange = "8.30-10.10", rawText = "История"),
                ),
            ),
            lessonMapper = NovsuLessonMapper(
                clock = Clock.fixed(Instant.parse("2026-04-26T00:00:00Z"), ZoneOffset.UTC),
            ),
        )

        val templates = repository.getLessons(Group("College", 1, "Course", "ИСП-1", "schedule.xls"))

        assertEquals("08:30", templates[0].startTime.toString())
        assertEquals("10:10", templates[0].endTime.toString())
        assertEquals("08:30", templates[1].startTime.toString())
        assertEquals("09:30", templates[1].endTime.toString())
        assertEquals("09:40", templates[2].startTime.toString())
        assertEquals("10:40", templates[2].endTime.toString())
    }

    private fun rawLesson(dayOfWeek: String, timeRange: String, rawText: String) = NovsuRawLesson(
        groupName = "ИСП-1",
        dayOfWeek = dayOfWeek,
        timeRange = timeRange,
        rawText = rawText,
        weekType = WeekType.ALL,
    )

    private class FakeScheduleDataSource(private val lessons: List<NovsuRawLesson>) : NovsuScheduleDataSource {

        override suspend fun getGroups(): List<Group> = emptyList()

        override suspend fun getScheduleForGroup(groupName: String, xlsUrl: String): List<NovsuRawLesson> = lessons

        override suspend fun getCurrentWeekType(): WeekType? = null

        override suspend fun getWeekTypeForDate(date: LocalDate): WeekType? = null
    }
}

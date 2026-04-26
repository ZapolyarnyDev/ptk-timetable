package io.github.zapolyarnydev.ptktimetable.data.repository

import io.github.zapolyarnydev.ptktimetable.data.model.PtkCurrentWeekType
import io.github.zapolyarnydev.ptktimetable.data.model.PtkGroupInfo
import io.github.zapolyarnydev.ptktimetable.data.model.PtkRawLesson
import io.github.zapolyarnydev.ptktimetable.data.model.PtkWeekType
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekInfo
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekSource
import io.github.zapolyarnydev.ptktimetable.domain.schedule.service.WeekResolver
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class DomainTimetableRepositoryAdapterTest {

    @Test
    fun `getTemplatesByGroup remaps saturday portal slots to local saturday bell schedule`() = runBlocking {
        val adapter = DomainTimetableRepositoryAdapter(
            scheduleRepository = FakeScheduleRepository(
                lessons = listOf(
                    rawLesson(dayOfWeek = "сб", timeRange = "8.30-10.10", rawText = "Математика"),
                    rawLesson(dayOfWeek = "сб", timeRange = "10.20-12.00", rawText = "Физика"),
                    rawLesson(dayOfWeek = "пн", timeRange = "8.30-10.10", rawText = "История")
                )
            ),
            weekResolver = FakeWeekResolver(),
            clock = Clock.fixed(Instant.parse("2026-04-26T00:00:00Z"), ZoneOffset.UTC)
        )

        val templates = adapter.getTemplatesByGroup("ИСП-1")

        assertEquals("08:30", templates[0].startTime.toString())
        assertEquals("10:10", templates[0].endTime.toString())
        assertEquals("08:30", templates[1].startTime.toString())
        assertEquals("09:30", templates[1].endTime.toString())
        assertEquals("09:40", templates[2].startTime.toString())
        assertEquals("10:40", templates[2].endTime.toString())
    }

    private fun rawLesson(
        dayOfWeek: String,
        timeRange: String,
        rawText: String
    ) = PtkRawLesson(
        groupName = "ИСП-1",
        dayOfWeek = dayOfWeek,
        timeRange = timeRange,
        rawText = rawText,
        weekType = PtkWeekType.ALL
    )

    private class FakeScheduleRepository(
        private val lessons: List<PtkRawLesson>
    ) : ScheduleRepository {

        override suspend fun getGroups(): List<PtkGroupInfo> = emptyList()

        override suspend fun getScheduleForGroup(groupName: String): List<PtkRawLesson> = lessons

        override suspend fun getCurrentWeekType(): PtkCurrentWeekType = PtkCurrentWeekType.UNKNOWN

        override suspend fun getWeekTypeForDate(date: LocalDate): PtkCurrentWeekType =
            PtkCurrentWeekType.UNKNOWN
    }

    private class FakeWeekResolver : WeekResolver {
        override suspend fun resolve(date: LocalDate): WeekInfo =
            WeekInfo(date = date, isUpper = null, source = WeekSource.UNKNOWN)

        override suspend fun resolveRange(from: LocalDate, to: LocalDate): Map<LocalDate, WeekInfo> =
            emptyMap()
    }
}

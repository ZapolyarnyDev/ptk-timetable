package io.github.zapolyarnydev.ptktimetable.data.repository

import io.github.zapolyarnydev.ptktimetable.data.remote.xls.NovsuRawLesson
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Group
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekSource
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class PortalBackedWeekResolverTest {

    @Test
    fun `resolve uses portal parity relative to current week`() = runBlocking {
        val clock = Clock.fixed(Instant.parse("2026-03-16T00:00:00Z"), ZoneOffset.UTC)
        val resolver = PortalBackedWeekResolver(
            scheduleRepository = FakeScheduleRepository(WeekType.UPPER),
            clock = clock,
        )

        val today = LocalDate.of(2026, 3, 16)
        val nextWeek = LocalDate.of(2026, 3, 23)
        val infoToday = resolver.resolve(today)
        val infoNextWeek = resolver.resolve(nextWeek)

        assertEquals(WeekSource.PORTAL, infoToday.source)
        assertTrue(infoToday.isUpper == true)
        assertFalse(infoNextWeek.isUpper == true)
    }

    @Test
    fun `resolve falls back to local rule when portal week unknown`() = runBlocking {
        val resolver = PortalBackedWeekResolver(
            scheduleRepository = FakeScheduleRepository(null),
            clock = Clock.fixed(Instant.parse("2026-03-16T00:00:00Z"), ZoneOffset.UTC),
            fallbackReferenceDate = LocalDate.of(2026, 3, 16),
            fallbackReferenceIsUpper = true,
        )

        val info = resolver.resolve(LocalDate.of(2026, 3, 30))

        assertEquals(WeekSource.LOCAL_RULE, info.source)
        assertNotNull(info.isUpper)
    }

    private class FakeScheduleRepository(private val currentWeekType: WeekType?) : ScheduleRepository {

        override suspend fun getGroups(): List<Group> = emptyList()

        override suspend fun getScheduleForGroup(groupName: String): List<NovsuRawLesson> = emptyList()

        override suspend fun getCurrentWeekType(): WeekType? = currentWeekType

        override suspend fun getWeekTypeForDate(date: LocalDate): WeekType? = null
    }
}

package io.github.zapolyarnydev.ptktimetable.data.repository

import io.github.zapolyarnydev.ptktimetable.data.model.PtkCurrentWeekType
import io.github.zapolyarnydev.ptktimetable.data.model.PtkGroupInfo
import io.github.zapolyarnydev.ptktimetable.data.model.PtkRawLesson
import io.github.zapolyarnydev.ptktimetable.data.remote.html.PtkCurrentWeekHtmlParser
import io.github.zapolyarnydev.ptktimetable.data.remote.html.PtkGroupsHtmlParser
import io.github.zapolyarnydev.ptktimetable.data.remote.service.PortalService
import io.github.zapolyarnydev.ptktimetable.data.remote.service.PortalServiceImpl
import io.github.zapolyarnydev.ptktimetable.data.remote.xls.PtkXlsScheduleParser
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.Locale

class PtkScheduleRepository(
    private val portalService: PortalService = PortalServiceImpl(),
    private val groupsHtmlParser: PtkGroupsHtmlParser = PtkGroupsHtmlParser(),
    private val currentWeekHtmlParser: PtkCurrentWeekHtmlParser = PtkCurrentWeekHtmlParser(),
    private val xlsScheduleParser: PtkXlsScheduleParser = PtkXlsScheduleParser(),
    private val clock: Clock = Clock.systemDefaultZone(),
    private val calendarCacheTtl: Duration = Duration.ofMinutes(30)
) : ScheduleRepository {

    private val calendarCacheMutex = Mutex()
    private var calendarCache: CalendarCache? = null

    override suspend fun getGroups(): List<PtkGroupInfo> {
        val html = portalService.fetchPortalHtml()
        updateCalendarCache(html)
        return groupsHtmlParser.parseGroups(html, PortalServiceImpl.PORTAL_URL)
    }

    override suspend fun getScheduleForGroup(groupName: String): List<PtkRawLesson> {
        val normalizedGroupName = groupName.trim()
        if (normalizedGroupName.isBlank()) return emptyList()

        val groups = getGroups()
        val selectedGroup = groups.firstOrNull { sameGroup(it.groupName, normalizedGroupName) }
            ?: return emptyList()

        val xlsBytes = portalService.downloadXls(selectedGroup.xlsUrl)
        return xlsScheduleParser.parseSchedule(xlsBytes, selectedGroup.groupName)
    }

    override suspend fun getCurrentWeekType(): PtkCurrentWeekType {
        return getWeekTypeForDate(LocalDate.now())
    }

    override suspend fun getWeekTypeForDate(date: LocalDate): PtkCurrentWeekType {
        val html = getPortalHtmlForCalendar()
        return currentWeekHtmlParser.parseWeekTypeForDate(html, date)
    }

    private suspend fun getPortalHtmlForCalendar(): String {
        return calendarCacheMutex.withLock {
            val now = Instant.now(clock)
            val cached = calendarCache
            if (cached != null && Duration.between(cached.fetchedAt, now) <= calendarCacheTtl) {
                return@withLock cached.html
            }

            val freshHtml = portalService.fetchPortalHtml()
            calendarCache = CalendarCache(
                html = freshHtml,
                fetchedAt = now
            )
            freshHtml
        }
    }

    private suspend fun updateCalendarCache(html: String) {
        calendarCacheMutex.withLock {
            calendarCache = CalendarCache(
                html = html,
                fetchedAt = Instant.now(clock)
            )
        }
    }

    private fun sameGroup(left: String, right: String): Boolean {
        return left.trim().lowercase(Locale.ROOT) == right.trim().lowercase(Locale.ROOT)
    }

    private data class CalendarCache(
        val html: String,
        val fetchedAt: Instant
    )
}

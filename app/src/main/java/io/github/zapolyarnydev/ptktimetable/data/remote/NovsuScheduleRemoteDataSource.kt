package io.github.zapolyarnydev.ptktimetable.data.remote

import io.github.zapolyarnydev.ptktimetable.data.remote.html.PtkCurrentWeekHtmlParser
import io.github.zapolyarnydev.ptktimetable.data.remote.html.PtkGroupsHtmlParser
import io.github.zapolyarnydev.ptktimetable.data.remote.service.PortalService
import io.github.zapolyarnydev.ptktimetable.data.remote.service.PortalServiceImpl
import io.github.zapolyarnydev.ptktimetable.data.remote.xls.NovsuRawLesson
import io.github.zapolyarnydev.ptktimetable.data.remote.xls.PtkXlsScheduleParser
import io.github.zapolyarnydev.ptktimetable.data.remote.xls.ScheduleDocumentParser
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Group
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

class NovsuScheduleRemoteDataSource(
    private val portalService: PortalService = PortalServiceImpl(),
    private val groupsHtmlParser: PtkGroupsHtmlParser = PtkGroupsHtmlParser(),
    private val currentWeekHtmlParser: PtkCurrentWeekHtmlParser = PtkCurrentWeekHtmlParser(),
    private val xlsScheduleParser: ScheduleDocumentParser = PtkXlsScheduleParser(),
    private val clock: Clock = Clock.systemDefaultZone(),
    private val calendarCacheTtl: Duration = Duration.ofMinutes(30),
) : NovsuScheduleDataSource {

    private val calendarCacheMutex = Mutex()
    private var calendarCache: CalendarCache? = null

    override suspend fun getGroups(): List<Group> {
        val html = portalService.fetchPortalHtml()
        updateCalendarCache(html)
        return groupsHtmlParser.parseGroups(html, PortalServiceImpl.PORTAL_URL)
    }

    override suspend fun getScheduleForGroup(groupName: String, xlsUrl: String): List<NovsuRawLesson> {
        val normalizedGroupName = groupName.trim()
        val normalizedXlsUrl = xlsUrl.trim()
        if (normalizedGroupName.isBlank() || normalizedXlsUrl.isBlank()) return emptyList()
        val bytes = portalService.downloadXls(normalizedXlsUrl)
        return xlsScheduleParser.parseSchedule(bytes, normalizedGroupName)
    }

    override suspend fun getCurrentWeekType(): WeekType? = getWeekTypeForDate(LocalDate.now())

    override suspend fun getWeekTypeForDate(date: LocalDate): WeekType? {
        val html = getPortalHtmlForCalendar()
        return currentWeekHtmlParser.parseWeekTypeForDate(html, date)
    }

    private suspend fun getPortalHtmlForCalendar(): String = calendarCacheMutex.withLock {
        val now = Instant.now(clock)
        val cached = calendarCache
        if (cached != null && Duration.between(cached.fetchedAt, now) <= calendarCacheTtl) {
            return@withLock cached.html
        }
        val freshHtml = portalService.fetchPortalHtml()
        calendarCache = CalendarCache(freshHtml, now)
        freshHtml
    }

    private suspend fun updateCalendarCache(html: String) {
        calendarCacheMutex.withLock {
            calendarCache = CalendarCache(html, Instant.now(clock))
        }
    }

    private data class CalendarCache(val html: String, val fetchedAt: Instant)
}

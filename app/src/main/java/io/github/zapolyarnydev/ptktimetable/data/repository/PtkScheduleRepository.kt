package io.github.zapolyarnydev.ptktimetable.data.repository

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
import java.util.Locale

class PtkScheduleRepository(
    private val portalService: PortalService = PortalServiceImpl(),
    private val groupsHtmlParser: PtkGroupsHtmlParser = PtkGroupsHtmlParser(),
    private val currentWeekHtmlParser: PtkCurrentWeekHtmlParser = PtkCurrentWeekHtmlParser(),
    private val xlsScheduleParser: ScheduleDocumentParser = PtkXlsScheduleParser(),
    private val clock: Clock = Clock.systemDefaultZone(),
    private val calendarCacheTtl: Duration = Duration.ofMinutes(30),
) : ScheduleRepository {

    private val calendarCacheMutex = Mutex()
    private var calendarCache: CalendarCache? = null

    override suspend fun getGroups(): List<Group> {
        val html = portalService.fetchPortalHtml()
        updateCalendarCache(html)
        return groupsHtmlParser.parseGroups(html, PortalServiceImpl.PORTAL_URL)
    }

    override suspend fun getScheduleForGroup(groupName: String): List<NovsuRawLesson> {
        val normalizedGroupName = groupName.trim()
        if (normalizedGroupName.isBlank()) return emptyList()

        val groups = getGroups()
        val selectedGroup = groups.firstOrNull { sameGroup(it.groupName, normalizedGroupName) }
            ?: return emptyList()

        return downloadSchedule(selectedGroup.groupName, selectedGroup.sourceUrl)
    }

    override suspend fun getScheduleForGroup(groupName: String, xlsUrl: String): List<NovsuRawLesson> {
        val normalizedGroupName = groupName.trim()
        val normalizedXlsUrl = xlsUrl.trim()
        if (normalizedGroupName.isBlank() || normalizedXlsUrl.isBlank()) return emptyList()
        return downloadSchedule(normalizedGroupName, normalizedXlsUrl)
    }

    override suspend fun getCurrentWeekType(): WeekType? = getWeekTypeForDate(LocalDate.now())

    override suspend fun getWeekTypeForDate(date: LocalDate): WeekType? {
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
                fetchedAt = now,
            )
            freshHtml
        }
    }

    private suspend fun updateCalendarCache(html: String) {
        calendarCacheMutex.withLock {
            calendarCache = CalendarCache(
                html = html,
                fetchedAt = Instant.now(clock),
            )
        }
    }

    private suspend fun downloadSchedule(groupName: String, xlsUrl: String): List<NovsuRawLesson> {
        val xlsBytes = portalService.downloadXls(xlsUrl)
        return xlsScheduleParser.parseSchedule(xlsBytes, groupName)
    }

    private fun sameGroup(left: String, right: String): Boolean =
        left.trim().lowercase(Locale.ROOT) == right.trim().lowercase(Locale.ROOT)

    private data class CalendarCache(val html: String, val fetchedAt: Instant)
}

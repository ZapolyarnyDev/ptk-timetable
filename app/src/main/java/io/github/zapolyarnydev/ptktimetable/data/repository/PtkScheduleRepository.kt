package io.github.zapolyarnydev.ptktimetable.data.repository

import io.github.zapolyarnydev.ptktimetable.data.model.PtkCurrentWeekType
import io.github.zapolyarnydev.ptktimetable.data.model.PtkGroupInfo
import io.github.zapolyarnydev.ptktimetable.data.model.PtkRawLesson
import io.github.zapolyarnydev.ptktimetable.data.remote.html.PtkCurrentWeekHtmlParser
import io.github.zapolyarnydev.ptktimetable.data.remote.html.PtkGroupsHtmlParser
import io.github.zapolyarnydev.ptktimetable.data.remote.service.PortalService
import io.github.zapolyarnydev.ptktimetable.data.remote.service.PortalServiceImpl
import io.github.zapolyarnydev.ptktimetable.data.remote.xls.PtkXlsScheduleParser
import java.util.Locale

class PtkScheduleRepository(
    private val portalService: PortalService = PortalServiceImpl(),
    private val groupsHtmlParser: PtkGroupsHtmlParser = PtkGroupsHtmlParser(),
    private val currentWeekHtmlParser: PtkCurrentWeekHtmlParser = PtkCurrentWeekHtmlParser(),
    private val xlsScheduleParser: PtkXlsScheduleParser = PtkXlsScheduleParser()
) : ScheduleRepository {

    override suspend fun getGroups(): List<PtkGroupInfo> {
        val html = portalService.fetchPortalHtml()
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
        val html = portalService.fetchPortalHtml()
        return currentWeekHtmlParser.parseCurrentWeekType(html)
    }

    private fun sameGroup(left: String, right: String): Boolean {
        return left.trim().lowercase(Locale.ROOT) == right.trim().lowercase(Locale.ROOT)
    }
}

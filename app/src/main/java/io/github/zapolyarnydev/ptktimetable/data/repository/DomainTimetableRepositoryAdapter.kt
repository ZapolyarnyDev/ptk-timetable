package io.github.zapolyarnydev.ptktimetable.data.repository

import io.github.zapolyarnydev.ptktimetable.data.mapper.NovsuLessonMapper
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Group
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Lesson
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.RefreshResult
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekFilter
import io.github.zapolyarnydev.ptktimetable.domain.schedule.repository.TimetableRepository
import io.github.zapolyarnydev.ptktimetable.domain.schedule.service.WeekResolver
import io.github.zapolyarnydev.ptktimetable.domain.schedule.service.WeekRules
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate

class DomainTimetableRepositoryAdapter(
    private val scheduleRepository: ScheduleRepository = PtkScheduleRepository(),
    private val weekResolver: WeekResolver = PortalBackedWeekResolver(scheduleRepository),
    private val clock: Clock = Clock.systemDefaultZone(),
    private val lessonMapper: NovsuLessonMapper = NovsuLessonMapper(clock = clock),
) : TimetableRepository {

    override suspend fun refreshGroupsAndTemplates(): RefreshResult {
        val groups = scheduleRepository.getGroups()
        runCatching { weekResolver.resolve(LocalDate.now(clock)) }
        return RefreshResult(
            groupsCount = groups.size,
            refreshedAt = Instant.now(clock),
        )
    }

    override suspend fun getGroups(): List<Group> = scheduleRepository.getGroups()

    override suspend fun getTemplatesByGroup(groupName: String): List<Lesson> =
        lessonMapper.map(scheduleRepository.getScheduleForGroup(groupName))

    override suspend fun getTemplatesByGroup(groupName: String, sourceUrl: String): List<Lesson> =
        lessonMapper.map(scheduleRepository.getScheduleForGroup(groupName, sourceUrl))

    override suspend fun getOccurrencesByDate(groupName: String, date: LocalDate): List<Lesson> {
        val templates = getTemplatesByGroup(groupName)
            .filter { it.dayOfWeek == date.dayOfWeek }
        val weekInfo = weekResolver.resolve(date)
        val filteredTemplates = templates.filter { template ->
            WeekRules.matches(template.weekType, weekInfo.weekType)
        }

        return filteredTemplates.sortedBy { it.startTime }
    }

    override suspend fun getOccurrencesByDayOfWeek(
        groupName: String,
        dayOfWeek: DayOfWeek,
        weekFilter: WeekFilter,
        anchorDate: LocalDate,
    ): List<Lesson> {
        val templates = getTemplatesByGroup(groupName)
            .filter { it.dayOfWeek == dayOfWeek }
            .filter { template -> WeekRules.matches(template.weekType, weekFilter) }

        return templates.sortedBy { it.startTime }
    }
}

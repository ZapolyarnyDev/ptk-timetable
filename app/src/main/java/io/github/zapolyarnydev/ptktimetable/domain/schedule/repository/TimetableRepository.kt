package io.github.zapolyarnydev.ptktimetable.domain.schedule.repository

import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Group
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Lesson
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.RefreshResult
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekFilter
import java.time.DayOfWeek
import java.time.LocalDate

interface TimetableRepository {
    suspend fun refreshGroupsAndTemplates(): RefreshResult
    suspend fun getGroups(): List<Group>
    suspend fun getTemplatesByGroup(groupName: String): List<Lesson>
    suspend fun getTemplatesByGroup(groupName: String, sourceUrl: String): List<Lesson> = getTemplatesByGroup(groupName)
    suspend fun getOccurrencesByDate(groupName: String, date: LocalDate): List<Lesson>
    suspend fun getOccurrencesByDayOfWeek(
        groupName: String,
        dayOfWeek: DayOfWeek,
        weekFilter: WeekFilter,
        anchorDate: LocalDate,
    ): List<Lesson>
}

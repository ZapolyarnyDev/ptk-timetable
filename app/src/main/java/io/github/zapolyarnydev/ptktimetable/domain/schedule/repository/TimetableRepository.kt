package io.github.zapolyarnydev.ptktimetable.domain.schedule.repository

import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.LessonOccurrence
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.LessonTemplate
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.RefreshResult
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.TimetableGroup
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekFilter
import java.time.DayOfWeek
import java.time.LocalDate

interface TimetableRepository {
    suspend fun refreshGroupsAndTemplates(): RefreshResult
    suspend fun getGroups(): List<TimetableGroup>
    suspend fun getTemplatesByGroup(groupName: String): List<LessonTemplate>
    suspend fun getOccurrencesByDate(groupName: String, date: LocalDate): List<LessonOccurrence>
    suspend fun getOccurrencesByDayOfWeek(
        groupName: String,
        dayOfWeek: DayOfWeek,
        weekFilter: WeekFilter,
        anchorDate: LocalDate
    ): List<LessonOccurrence>
}


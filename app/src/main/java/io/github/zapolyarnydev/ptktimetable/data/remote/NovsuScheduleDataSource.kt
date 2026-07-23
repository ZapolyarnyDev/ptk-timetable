package io.github.zapolyarnydev.ptktimetable.data.remote

import io.github.zapolyarnydev.ptktimetable.data.remote.xls.NovsuRawLesson
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Group
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType
import java.time.LocalDate

interface NovsuScheduleDataSource {
    suspend fun getGroups(): List<Group>
    suspend fun getScheduleForGroup(groupName: String, xlsUrl: String): List<NovsuRawLesson>
    suspend fun getCurrentWeekType(): WeekType?
    suspend fun getWeekTypeForDate(date: LocalDate): WeekType?
}

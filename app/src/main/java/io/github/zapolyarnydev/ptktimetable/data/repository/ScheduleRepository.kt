package io.github.zapolyarnydev.ptktimetable.data.repository

import io.github.zapolyarnydev.ptktimetable.data.model.PtkCurrentWeekType
import io.github.zapolyarnydev.ptktimetable.data.model.PtkGroupInfo
import io.github.zapolyarnydev.ptktimetable.data.model.PtkRawLesson
import java.time.LocalDate

interface ScheduleRepository {
    suspend fun getGroups(): List<PtkGroupInfo>
    suspend fun getScheduleForGroup(groupName: String): List<PtkRawLesson>
    suspend fun getCurrentWeekType(): PtkCurrentWeekType
    suspend fun getWeekTypeForDate(date: LocalDate): PtkCurrentWeekType
}

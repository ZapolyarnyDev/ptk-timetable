package io.github.zapolyarnydev.ptktimetable.domain.schedule.service

import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekInfo
import java.time.LocalDate

interface WeekResolver {
    suspend fun resolve(date: LocalDate): WeekInfo
    suspend fun resolveRange(from: LocalDate, to: LocalDate): Map<LocalDate, WeekInfo>
}


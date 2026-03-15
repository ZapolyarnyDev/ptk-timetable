package io.github.zapolyarnydev.ptktimetable.domain.schedule.usecase

import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekInfo
import io.github.zapolyarnydev.ptktimetable.domain.schedule.service.WeekResolver
import java.time.LocalDate

class ResolveWeekInfoUseCase(
    private val weekResolver: WeekResolver
) {
    suspend operator fun invoke(date: LocalDate): WeekInfo = weekResolver.resolve(date)
}


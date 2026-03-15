package io.github.zapolyarnydev.ptktimetable.domain.schedule.usecase

import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.LessonOccurrence
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekFilter
import io.github.zapolyarnydev.ptktimetable.domain.schedule.repository.TimetableRepository
import java.time.DayOfWeek
import java.time.LocalDate

class GetScheduleByDayUseCase(
    private val repository: TimetableRepository
) {
    suspend operator fun invoke(
        groupName: String,
        dayOfWeek: DayOfWeek,
        weekFilter: WeekFilter,
        anchorDate: LocalDate
    ): List<LessonOccurrence> {
        return repository.getOccurrencesByDayOfWeek(groupName, dayOfWeek, weekFilter, anchorDate)
    }
}


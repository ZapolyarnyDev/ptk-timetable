package io.github.zapolyarnydev.ptktimetable.domain.schedule.usecase

import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.LessonOccurrence
import io.github.zapolyarnydev.ptktimetable.domain.schedule.repository.TimetableRepository
import java.time.LocalDate

class GetScheduleByDateUseCase(
    private val repository: TimetableRepository
) {
    suspend operator fun invoke(
        groupName: String,
        date: LocalDate
    ): List<LessonOccurrence> {
        return repository.getOccurrencesByDate(groupName, date)
    }
}


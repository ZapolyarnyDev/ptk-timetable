package io.github.zapolyarnydev.ptktimetable.domain.schedule.usecase

import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Lesson
import io.github.zapolyarnydev.ptktimetable.domain.schedule.repository.TimetableRepository

class GetTemplatesByGroupUseCase(private val repository: TimetableRepository) {
    suspend operator fun invoke(groupName: String): List<Lesson> = repository.getTemplatesByGroup(groupName)
}

package io.github.zapolyarnydev.ptktimetable.domain.schedule.usecase

import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.LessonTemplate
import io.github.zapolyarnydev.ptktimetable.domain.schedule.repository.TimetableRepository

class GetTemplatesByGroupUseCase(
    private val repository: TimetableRepository
) {
    suspend operator fun invoke(groupName: String): List<LessonTemplate> {
        return repository.getTemplatesByGroup(groupName)
    }
}


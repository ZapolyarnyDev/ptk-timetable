package io.github.zapolyarnydev.ptktimetable.domain.schedule.usecase

import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Group
import io.github.zapolyarnydev.ptktimetable.domain.schedule.repository.TimetableRepository

class GetGroupsUseCase(private val repository: TimetableRepository) {
    suspend operator fun invoke(): List<Group> = repository.getGroups()
}

package io.github.zapolyarnydev.ptktimetable.domain.schedule.usecase

import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.TimetableGroup
import io.github.zapolyarnydev.ptktimetable.domain.schedule.repository.TimetableRepository

class GetGroupsUseCase(
    private val repository: TimetableRepository
) {
    suspend operator fun invoke(): List<TimetableGroup> = repository.getGroups()
}


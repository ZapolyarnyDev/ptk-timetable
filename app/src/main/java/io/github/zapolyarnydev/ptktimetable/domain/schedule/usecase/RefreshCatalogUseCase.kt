package io.github.zapolyarnydev.ptktimetable.domain.schedule.usecase

import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.RefreshResult
import io.github.zapolyarnydev.ptktimetable.domain.schedule.repository.TimetableRepository

class RefreshCatalogUseCase(
    private val repository: TimetableRepository
) {
    suspend operator fun invoke(): RefreshResult = repository.refreshGroupsAndTemplates()
}


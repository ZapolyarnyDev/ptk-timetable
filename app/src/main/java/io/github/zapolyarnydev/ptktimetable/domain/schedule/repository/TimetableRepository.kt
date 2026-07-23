package io.github.zapolyarnydev.ptktimetable.domain.schedule.repository

import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Group
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Lesson

interface TimetableRepository {
    suspend fun getGroups(): List<Group>
    suspend fun getLessons(group: Group): List<Lesson>
}

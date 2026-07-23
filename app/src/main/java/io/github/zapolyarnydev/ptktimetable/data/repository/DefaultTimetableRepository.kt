package io.github.zapolyarnydev.ptktimetable.data.repository

import io.github.zapolyarnydev.ptktimetable.data.mapper.NovsuLessonMapper
import io.github.zapolyarnydev.ptktimetable.data.remote.NovsuScheduleDataSource
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Group
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Lesson
import io.github.zapolyarnydev.ptktimetable.domain.schedule.repository.TimetableRepository

class DefaultTimetableRepository(
    private val remoteDataSource: NovsuScheduleDataSource,
    private val lessonMapper: NovsuLessonMapper,
) : TimetableRepository {

    override suspend fun getGroups(): List<Group> = remoteDataSource.getGroups()

    override suspend fun getLessons(group: Group): List<Lesson> =
        lessonMapper.map(remoteDataSource.getScheduleForGroup(group.groupName, group.sourceUrl))
}

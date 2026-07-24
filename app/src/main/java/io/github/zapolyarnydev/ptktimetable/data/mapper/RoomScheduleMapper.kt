package io.github.zapolyarnydev.ptktimetable.data.mapper

import io.github.zapolyarnydev.ptktimetable.data.local.database.entity.GroupEntity
import io.github.zapolyarnydev.ptktimetable.data.local.database.entity.LessonTemplateEntity
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Group
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Lesson
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime

class RoomScheduleMapper {

    fun toEntity(group: Group): GroupEntity = GroupEntity(
        groupName = group.groupName,
        collegeName = group.collegeName,
        course = group.course,
        courseName = group.courseName,
        sourceUrl = group.sourceUrl,
    )

    fun toDomain(group: GroupEntity): Group = Group(
        collegeName = group.collegeName,
        course = group.course,
        courseName = group.courseName,
        groupName = group.groupName,
        sourceUrl = group.sourceUrl,
    )

    fun toEntity(lesson: Lesson): LessonTemplateEntity = LessonTemplateEntity(
        id = lesson.id,
        groupName = lesson.groupName,
        dayOfWeek = lesson.dayOfWeek.value,
        startTime = lesson.startTime.toString(),
        endTime = lesson.endTime.toString(),
        weekType = lesson.weekType.name,
        subject = lesson.subject,
        teacher = lesson.teacher,
        room = lesson.room,
        rawText = lesson.rawText,
        sourceUpdatedAtEpochMillis = lesson.sourceUpdatedAt.toEpochMilli(),
    )

    fun toDomain(lesson: LessonTemplateEntity): Lesson? {
        val dayOfWeek = runCatching { DayOfWeek.of(lesson.dayOfWeek) }.getOrNull() ?: return null
        val startTime = runCatching { LocalTime.parse(lesson.startTime) }.getOrNull() ?: return null
        val endTime = runCatching { LocalTime.parse(lesson.endTime) }.getOrNull() ?: return null
        val weekType = runCatching { WeekType.valueOf(lesson.weekType) }.getOrNull() ?: return null
        return Lesson(
            id = lesson.id,
            groupName = lesson.groupName,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            weekType = weekType,
            subject = lesson.subject,
            teacher = lesson.teacher,
            room = lesson.room,
            rawText = lesson.rawText,
            sourceUpdatedAt = Instant.ofEpochMilli(lesson.sourceUpdatedAtEpochMillis),
        )
    }
}

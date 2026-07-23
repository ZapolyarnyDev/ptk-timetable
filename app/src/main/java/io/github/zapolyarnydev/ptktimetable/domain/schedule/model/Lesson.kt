package io.github.zapolyarnydev.ptktimetable.domain.schedule.model

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime

data class Lesson(
    val id: String,
    val groupName: String,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val weekType: WeekType,
    val subject: String,
    val teacher: String?,
    val room: String?,
    val rawText: String,
    val sourceUpdatedAt: Instant,
)

package io.github.zapolyarnydev.ptktimetable.domain.schedule.model

import java.time.LocalDate
import java.time.LocalDateTime

data class LessonOccurrence(
    val templateId: String,
    val groupName: String,
    val date: LocalDate,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val weekTypeResolved: WeekType,
    val subject: String,
    val teacher: String?,
    val room: String?,
    val rawText: String
)


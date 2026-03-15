package io.github.zapolyarnydev.ptktimetable.domain.schedule.model

data class TimetableGroup(
    val collegeName: String,
    val course: Int,
    val courseName: String,
    val groupName: String,
    val sourceUrl: String
)


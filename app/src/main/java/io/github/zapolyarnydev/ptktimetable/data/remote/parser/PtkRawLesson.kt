package io.github.zapolyarnydev.ptktimetable.data.remote.parser

data class PtkRawLesson(
    val groupName: String,
    val dayOfWeek: String,
    val timeRange: String,
    val rawText: String,
    val weekType: PtkWeekType
)

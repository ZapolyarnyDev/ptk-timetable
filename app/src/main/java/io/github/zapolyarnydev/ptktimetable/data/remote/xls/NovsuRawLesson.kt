package io.github.zapolyarnydev.ptktimetable.data.remote.xls

import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType

data class NovsuRawLesson(
    val groupName: String,
    val dayOfWeek: String,
    val timeRange: String,
    val rawText: String,
    val weekType: WeekType,
)

package io.github.zapolyarnydev.ptktimetable.domain.schedule.model

import java.time.LocalDate

data class WeekInfo(
    val date: LocalDate,
    val isUpper: Boolean?,
    val source: WeekSource
)


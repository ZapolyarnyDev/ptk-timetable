package io.github.zapolyarnydev.ptktimetable.domain.schedule.model

import java.time.LocalDate

data class WeekInfo(val date: LocalDate, val weekType: WeekType?, val source: WeekSource) {
    val isUpper: Boolean?
        get() = when (weekType) {
            WeekType.UPPER -> true
            WeekType.LOWER -> false
            WeekType.ALL, null -> null
        }
}

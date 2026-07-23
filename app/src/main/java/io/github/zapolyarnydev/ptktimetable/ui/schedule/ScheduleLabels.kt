package io.github.zapolyarnydev.ptktimetable.ui.schedule

import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.ScheduleMode
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekFilter

internal val ScheduleMode.title: String
    get() = when (this) {
        ScheduleMode.BY_DAY -> "По дням"
        ScheduleMode.BY_DATE -> "По дате"
    }

internal val WeekFilter.title: String
    get() = when (this) {
        WeekFilter.ALL -> "Обе"
        WeekFilter.UPPER -> "Верхняя"
        WeekFilter.LOWER -> "Нижняя"
    }

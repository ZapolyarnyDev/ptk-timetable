package io.github.zapolyarnydev.ptktimetable.domain.schedule.service

import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekFilter
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType

object WeekRules {

    fun matches(lessonWeekType: WeekType, activeWeekType: WeekType?): Boolean {
        if (lessonWeekType == WeekType.ALL || activeWeekType == null || activeWeekType == WeekType.ALL) return true
        return lessonWeekType == activeWeekType
    }

    fun matches(lessonWeekType: WeekType, filter: WeekFilter): Boolean = when (filter) {
        WeekFilter.ALL -> true
        WeekFilter.UPPER -> lessonWeekType == WeekType.UPPER || lessonWeekType == WeekType.ALL
        WeekFilter.LOWER -> lessonWeekType == WeekType.LOWER || lessonWeekType == WeekType.ALL
    }
}

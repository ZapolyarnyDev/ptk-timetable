package io.github.zapolyarnydev.ptktimetable.ui.schedule

import java.time.DayOfWeek

internal fun dayOfWeekToScheduleDay(dayOfWeek: DayOfWeek): ScheduleDay = when (dayOfWeek) {
    DayOfWeek.MONDAY -> ScheduleDay.MONDAY
    DayOfWeek.TUESDAY -> ScheduleDay.TUESDAY
    DayOfWeek.WEDNESDAY -> ScheduleDay.WEDNESDAY
    DayOfWeek.THURSDAY -> ScheduleDay.THURSDAY
    DayOfWeek.FRIDAY -> ScheduleDay.FRIDAY
    DayOfWeek.SATURDAY -> ScheduleDay.SATURDAY
    DayOfWeek.SUNDAY -> ScheduleDay.SUNDAY
}

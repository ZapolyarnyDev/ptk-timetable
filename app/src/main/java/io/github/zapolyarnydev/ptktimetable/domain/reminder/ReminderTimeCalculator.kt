package io.github.zapolyarnydev.ptktimetable.domain.reminder

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class ReminderTimeCalculator(private val zoneId: ZoneId) {

    fun calculate(lessonDate: LocalDate, lessonStartTime: LocalTime, minutesBefore: Int): Instant {
        require(minutesBefore > 0)
        return lessonDate
            .atTime(lessonStartTime)
            .minusMinutes(minutesBefore.toLong())
            .atZone(zoneId)
            .toInstant()
    }

    fun isFuture(triggerAt: Instant, now: Instant): Boolean = triggerAt.isAfter(now)
}

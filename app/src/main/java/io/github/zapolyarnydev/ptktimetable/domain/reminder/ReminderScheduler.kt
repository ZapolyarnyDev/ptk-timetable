package io.github.zapolyarnydev.ptktimetable.domain.reminder

data class ScheduledReminder(val id: String, val triggerAtEpochMillis: Long, val title: String, val message: String)

interface ReminderScheduler {
    fun schedule(reminder: ScheduledReminder)

    fun cancel(reminderId: String)
}

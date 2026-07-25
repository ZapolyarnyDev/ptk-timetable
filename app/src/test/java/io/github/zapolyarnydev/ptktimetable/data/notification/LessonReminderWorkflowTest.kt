package io.github.zapolyarnydev.ptktimetable.data.notification

import io.github.zapolyarnydev.ptktimetable.data.local.LessonNote
import io.github.zapolyarnydev.ptktimetable.domain.reminder.ReminderScheduler
import io.github.zapolyarnydev.ptktimetable.domain.reminder.ReminderTimeCalculator
import io.github.zapolyarnydev.ptktimetable.domain.reminder.ScheduledReminder
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

class LessonReminderWorkflowTest {

    private val scheduler = FakeReminderScheduler()
    private val workflow = LessonReminderWorkflow(
        scheduler = scheduler,
        timeCalculator = ReminderTimeCalculator(ZoneId.of("Europe/Moscow")),
        clock = Clock.fixed(Instant.parse("2026-07-25T06:00:00Z"), ZoneOffset.UTC),
        idFactory = { "reminder-1" },
    )

    @Test
    fun `create assigns reminder id and schedules calculated instant`() {
        val updated = workflow.create(note(), minutesBefore = 15)

        assertEquals("reminder-1", updated.reminderId)
        assertEquals(Instant.parse("2026-07-25T07:05:00Z").toEpochMilli(), updated.remindAtEpochMillis)
        assertTrue(updated.reminderEnabled)
        assertEquals("reminder-1", scheduler.scheduled.single().id)
    }

    @Test
    fun `change keeps reminder id and cancel clears persisted state`() {
        val created = workflow.create(note(), minutesBefore = 15)
        val changed = workflow.change(created, minutesBefore = 30)
        val cancelled = workflow.cancel(changed)

        assertEquals("reminder-1", changed.reminderId)
        assertEquals("reminder-1", scheduler.cancelled.single())
        assertNull(cancelled.reminderId)
        assertNull(cancelled.remindAtEpochMillis)
        assertFalse(cancelled.reminderEnabled)
    }

    private fun note() = LessonNote(
        id = "note-1",
        groupName = "ISP-1",
        date = LocalDate.of(2026, 7, 25),
        startTime = LocalTime.of(10, 20),
        endTime = LocalTime.NOON,
        weekType = WeekType.ALL,
        subject = "Math",
        teacher = null,
        classroom = null,
        rawText = "Math",
        noteText = "Read",
        reminderId = null,
        reminderEnabled = false,
        reminderMinutes = null,
        remindAtEpochMillis = null,
        createdAtEpochMillis = 1L,
    )

    private class FakeReminderScheduler : ReminderScheduler {
        val scheduled = mutableListOf<ScheduledReminder>()
        val cancelled = mutableListOf<String>()

        override fun schedule(reminder: ScheduledReminder) {
            scheduled += reminder
        }

        override fun cancel(reminderId: String) {
            cancelled += reminderId
        }
    }
}

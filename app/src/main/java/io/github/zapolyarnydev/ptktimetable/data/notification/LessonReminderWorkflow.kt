package io.github.zapolyarnydev.ptktimetable.data.notification

import io.github.zapolyarnydev.ptktimetable.data.local.LessonNote
import io.github.zapolyarnydev.ptktimetable.domain.reminder.ReminderScheduler
import io.github.zapolyarnydev.ptktimetable.domain.reminder.ReminderTimeCalculator
import io.github.zapolyarnydev.ptktimetable.domain.reminder.ScheduledReminder
import java.time.Clock
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID

class LessonReminderWorkflow(
    private val scheduler: ReminderScheduler,
    private val timeCalculator: ReminderTimeCalculator,
    private val clock: Clock = Clock.systemUTC(),
    private val idFactory: () -> String = { UUID.randomUUID().toString() },
) {

    fun create(note: LessonNote, minutesBefore: Int): LessonNote = schedule(
        note = note,
        reminderId = idFactory(),
        minutesBefore = minutesBefore,
    )

    fun change(note: LessonNote, minutesBefore: Int): LessonNote = schedule(
        note = note,
        reminderId = note.reminderId ?: idFactory(),
        minutesBefore = minutesBefore,
    )

    fun cancel(note: LessonNote): LessonNote {
        note.reminderId?.let(scheduler::cancel)
        return note.copy(
            reminderId = null,
            reminderEnabled = false,
            reminderMinutes = null,
            remindAtEpochMillis = null,
        )
    }

    fun reschedule(note: LessonNote): LessonNote {
        val reminderId = note.reminderId
        val triggerAt = note.remindAtEpochMillis?.let(Instant::ofEpochMilli)
        if (!note.reminderEnabled || reminderId == null || triggerAt == null) {
            return cancel(note)
        }
        if (!timeCalculator.isFuture(triggerAt, Instant.now(clock))) {
            return cancel(note)
        }
        scheduler.schedule(note.toScheduledReminder(reminderId, triggerAt))
        return note
    }

    private fun schedule(note: LessonNote, reminderId: String, minutesBefore: Int): LessonNote {
        val triggerAt = timeCalculator.calculate(
            lessonDate = note.date,
            lessonStartTime = note.startTime,
            minutesBefore = minutesBefore,
        )
        if (!timeCalculator.isFuture(triggerAt, Instant.now(clock))) {
            throw ReminderTimeUnavailableException()
        }
        val updated = note.copy(
            reminderId = reminderId,
            reminderEnabled = true,
            reminderMinutes = minutesBefore,
            remindAtEpochMillis = triggerAt.toEpochMilli(),
        )
        scheduler.schedule(updated.toScheduledReminder(reminderId, triggerAt))
        return updated
    }

    private fun LessonNote.toScheduledReminder(reminderId: String, triggerAt: Instant) = ScheduledReminder(
        id = reminderId,
        triggerAtEpochMillis = triggerAt.toEpochMilli(),
        title = "Скоро пара: ${subject.ifBlank { "занятие" }}",
        message = buildMessage(),
    )

    private fun LessonNote.buildMessage(): String {
        val base = "Группа $groupName, $timeRange, ${date.format(DATE_FORMATTER)}"
        return noteText.trim()
            .takeIf { it.isNotBlank() }
            ?.let { "$base\nЗаметка: $it" }
            ?: base
    }

    private companion object {
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }
}

class ReminderTimeUnavailableException :
    IllegalStateException("Слишком поздно для уведомления, увеличьте время напоминания")

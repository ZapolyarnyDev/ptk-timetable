package io.github.zapolyarnydev.ptktimetable.data.local

import io.github.zapolyarnydev.ptktimetable.data.local.database.dao.LessonNoteDao
import io.github.zapolyarnydev.ptktimetable.data.local.database.entity.LessonNoteEntity
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class LessonNote(
    val id: String,
    val groupName: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val weekType: WeekType,
    val subject: String,
    val teacher: String?,
    val classroom: String?,
    val rawText: String,
    val noteText: String,
    val reminderEnabled: Boolean,
    val reminderMinutes: Int?,
    val remindAtEpochMillis: Long?,
    val createdAtEpochMillis: Long,
) {
    val timeRange: String
        get() = "${TIME_FORMATTER.format(startTime)}-${TIME_FORMATTER.format(endTime)}"

    private companion object {
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("H.mm")
    }
}

class LessonNotesStore(private val dao: LessonNoteDao) {

    fun observeAll(): Flow<List<LessonNote>> = dao.observeAll().map { notes ->
        notes.mapNotNull(::toDomain)
    }

    fun observeByGroupAndDate(groupName: String, date: LocalDate): Flow<List<LessonNote>> =
        dao.observeByGroupAndDate(groupName, date.toEpochDay()).map { notes ->
            notes.mapNotNull(::toDomain)
        }

    suspend fun getAll(): List<LessonNote> = dao.getAll().mapNotNull(::toDomain)

    suspend fun getById(id: String): LessonNote? = dao.getById(id)?.let(::toDomain)

    suspend fun getByGroupAndDate(groupName: String, date: LocalDate): List<LessonNote> =
        dao.getByGroupAndDate(groupName, date.toEpochDay()).mapNotNull(::toDomain)

    suspend fun findForLesson(
        groupName: String,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        weekType: WeekType,
        rawText: String,
    ): LessonNote? = dao.findForLesson(
        groupName = groupName,
        dateEpochDay = date.toEpochDay(),
        startMinute = startTime.toMinuteOfDay(),
        endMinute = endTime.toMinuteOfDay(),
        weekType = weekType.name,
        rawText = rawText,
    )?.let(::toDomain)

    suspend fun upsert(note: LessonNote) {
        dao.upsert(note.toEntity())
    }

    suspend fun remove(noteId: String) {
        dao.delete(noteId)
    }

    fun newId(): String = UUID.randomUUID().toString()

    private fun LessonNote.toEntity(): LessonNoteEntity = LessonNoteEntity(
        id = id,
        groupName = groupName,
        dateEpochDay = date.toEpochDay(),
        startMinute = startTime.toMinuteOfDay(),
        endMinute = endTime.toMinuteOfDay(),
        weekType = weekType.name,
        subject = subject,
        teacher = teacher,
        room = classroom,
        rawText = rawText,
        noteText = noteText,
        reminderEnabled = reminderEnabled,
        reminderMinutes = reminderMinutes,
        remindAtEpochMillis = remindAtEpochMillis,
        createdAtEpochMillis = createdAtEpochMillis,
    )

    private fun toDomain(entity: LessonNoteEntity): LessonNote? {
        val date = runCatching { LocalDate.ofEpochDay(entity.dateEpochDay) }.getOrNull() ?: return null
        val startTime = entity.startMinute.toLocalTimeOrNull() ?: return null
        val endTime = entity.endMinute.toLocalTimeOrNull() ?: return null
        val weekType = runCatching { WeekType.valueOf(entity.weekType) }.getOrNull() ?: return null
        return LessonNote(
            id = entity.id,
            groupName = entity.groupName,
            date = date,
            startTime = startTime,
            endTime = endTime,
            weekType = weekType,
            subject = entity.subject,
            teacher = entity.teacher,
            classroom = entity.room,
            rawText = entity.rawText,
            noteText = entity.noteText,
            reminderEnabled = entity.reminderEnabled,
            reminderMinutes = entity.reminderMinutes,
            remindAtEpochMillis = entity.remindAtEpochMillis,
            createdAtEpochMillis = entity.createdAtEpochMillis,
        )
    }

    private fun LocalTime.toMinuteOfDay(): Int = hour * MINUTES_PER_HOUR + minute

    private fun Int.toLocalTimeOrNull(): LocalTime? {
        if (this !in 0 until MINUTES_PER_DAY) return null
        return LocalTime.of(this / MINUTES_PER_HOUR, this % MINUTES_PER_HOUR)
    }

    private companion object {
        const val MINUTES_PER_HOUR = 60
        const val MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR
    }
}

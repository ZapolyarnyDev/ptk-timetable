package io.github.zapolyarnydev.ptktimetable.data.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lesson_notes",
    indices = [
        Index(value = ["groupName", "dateEpochDay"]),
        Index(value = ["groupName", "dateEpochDay", "startMinute", "endMinute"]),
    ],
)
data class LessonNoteEntity(
    @PrimaryKey val id: String,
    val groupName: String,
    val dateEpochDay: Long,
    val startMinute: Int,
    val endMinute: Int,
    val weekType: String,
    val subject: String,
    val teacher: String?,
    val room: String?,
    val rawText: String,
    val noteText: String,
    val reminderEnabled: Boolean,
    val reminderMinutes: Int?,
    val remindAtEpochMillis: Long?,
    val createdAtEpochMillis: Long,
)

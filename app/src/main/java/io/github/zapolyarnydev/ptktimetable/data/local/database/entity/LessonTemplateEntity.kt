package io.github.zapolyarnydev.ptktimetable.data.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lesson_templates",
    indices = [
        Index(value = ["groupName"]),
        Index(value = ["groupName", "dayOfWeek", "startTime"]),
    ],
)
data class LessonTemplateEntity(
    @PrimaryKey val id: String,
    val groupName: String,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val weekType: String,
    val subject: String,
    val teacher: String?,
    val room: String?,
    val rawText: String,
    val sourceUpdatedAtEpochMillis: Long,
)

package io.github.zapolyarnydev.ptktimetable.data.local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.github.zapolyarnydev.ptktimetable.data.local.database.entity.LessonNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonNoteDao {

    @Query("SELECT * FROM lesson_notes ORDER BY dateEpochDay, startMinute, createdAtEpochMillis")
    fun observeAll(): Flow<List<LessonNoteEntity>>

    @Query("SELECT * FROM lesson_notes ORDER BY dateEpochDay, startMinute, createdAtEpochMillis")
    suspend fun getAll(): List<LessonNoteEntity>

    @Query(
        """
        SELECT * FROM lesson_notes
        WHERE groupName = :groupName AND dateEpochDay = :dateEpochDay
        ORDER BY startMinute, createdAtEpochMillis
        """,
    )
    fun observeByGroupAndDate(groupName: String, dateEpochDay: Long): Flow<List<LessonNoteEntity>>

    @Query(
        """
        SELECT * FROM lesson_notes
        WHERE groupName = :groupName AND dateEpochDay = :dateEpochDay
        ORDER BY startMinute, createdAtEpochMillis
        """,
    )
    suspend fun getByGroupAndDate(groupName: String, dateEpochDay: Long): List<LessonNoteEntity>

    @Query("SELECT * FROM lesson_notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): LessonNoteEntity?

    @Upsert
    suspend fun upsert(note: LessonNoteEntity)

    @Query("DELETE FROM lesson_notes WHERE id = :id")
    suspend fun delete(id: String)
}

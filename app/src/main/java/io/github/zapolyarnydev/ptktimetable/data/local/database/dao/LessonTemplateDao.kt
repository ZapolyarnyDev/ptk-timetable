package io.github.zapolyarnydev.ptktimetable.data.local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import io.github.zapolyarnydev.ptktimetable.data.local.database.entity.LessonTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonTemplateDao {

    @Query(
        """
        SELECT * FROM lesson_templates
        WHERE groupName = :groupName
        ORDER BY dayOfWeek, startTime
        """,
    )
    fun observeByGroup(groupName: String): Flow<List<LessonTemplateEntity>>

    @Query(
        """
        SELECT * FROM lesson_templates
        WHERE groupName = :groupName
        ORDER BY dayOfWeek, startTime
        """,
    )
    suspend fun getByGroup(groupName: String): List<LessonTemplateEntity>

    @Query("DELETE FROM lesson_templates WHERE groupName = :groupName")
    suspend fun deleteByGroup(groupName: String)

    @Upsert
    suspend fun upsertAll(lessons: List<LessonTemplateEntity>)

    @Transaction
    suspend fun replaceForGroup(groupName: String, lessons: List<LessonTemplateEntity>) {
        deleteByGroup(groupName)
        upsertAll(lessons)
    }
}

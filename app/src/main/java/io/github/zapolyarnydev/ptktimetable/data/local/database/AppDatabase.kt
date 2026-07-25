package io.github.zapolyarnydev.ptktimetable.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.zapolyarnydev.ptktimetable.data.local.database.dao.GroupDao
import io.github.zapolyarnydev.ptktimetable.data.local.database.dao.LessonNoteDao
import io.github.zapolyarnydev.ptktimetable.data.local.database.dao.LessonTemplateDao
import io.github.zapolyarnydev.ptktimetable.data.local.database.dao.SyncMetadataDao
import io.github.zapolyarnydev.ptktimetable.data.local.database.entity.GroupEntity
import io.github.zapolyarnydev.ptktimetable.data.local.database.entity.LessonNoteEntity
import io.github.zapolyarnydev.ptktimetable.data.local.database.entity.LessonTemplateEntity
import io.github.zapolyarnydev.ptktimetable.data.local.database.entity.SyncMetadataEntity

@Database(
    entities = [
        GroupEntity::class,
        LessonTemplateEntity::class,
        SyncMetadataEntity::class,
        LessonNoteEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao

    abstract fun lessonTemplateDao(): LessonTemplateDao

    abstract fun syncMetadataDao(): SyncMetadataDao

    abstract fun lessonNoteDao(): LessonNoteDao
}

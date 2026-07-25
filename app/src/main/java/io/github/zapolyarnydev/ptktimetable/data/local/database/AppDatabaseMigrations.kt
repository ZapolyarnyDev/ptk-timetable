package io.github.zapolyarnydev.ptktimetable.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE lesson_notes ADD COLUMN reminderId TEXT")
        database.execSQL(
            """
            UPDATE lesson_notes
            SET reminderId = id
            WHERE reminderEnabled = 1
            """.trimIndent(),
        )
    }
}

package io.github.zapolyarnydev.ptktimetable.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.zapolyarnydev.ptktimetable.data.local.database.entity.GroupEntity
import io.github.zapolyarnydev.ptktimetable.data.local.database.entity.LessonNoteEntity
import io.github.zapolyarnydev.ptktimetable.data.local.database.entity.LessonTemplateEntity
import io.github.zapolyarnydev.ptktimetable.data.local.database.entity.SyncMetadataEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseDaoTest {

    private lateinit var database: AppDatabase

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun groupsAreStoredAndObserved() = runBlocking {
        val group = group()

        database.groupDao().saveAll(listOf(group))

        assertEquals(listOf(group), database.groupDao().observeAll().first())
        assertEquals(group, database.groupDao().getByName(group.groupName))
    }

    @Test
    fun lessonsAreReplacedForOnlyRequestedGroup() = runBlocking {
        val first = lesson(id = "first", groupName = "ISP-1")
        val other = lesson(id = "other", groupName = "ISP-2")
        database.lessonTemplateDao().replaceForGroup("ISP-1", listOf(first))
        database.lessonTemplateDao().replaceForGroup("ISP-2", listOf(other))

        val replacement = lesson(id = "replacement", groupName = "ISP-1")
        database.lessonTemplateDao().replaceForGroup("ISP-1", listOf(replacement))

        assertEquals(listOf(replacement), database.lessonTemplateDao().getByGroup("ISP-1"))
        assertEquals(listOf(other), database.lessonTemplateDao().getByGroup("ISP-2"))
    }

    @Test
    fun syncMetadataIsUpserted() = runBlocking {
        val dao = database.syncMetadataDao()
        dao.upsert(SyncMetadataEntity("groups", 100L))
        dao.upsert(SyncMetadataEntity("groups", 200L))

        assertEquals(200L, dao.get("groups")?.lastSuccessfulSyncEpochMillis)
        assertNull(dao.get("missing"))
    }

    @Test
    fun notesAreQueriedByGroupAndDate() = runBlocking {
        val target = note(id = "target", groupName = "ISP-1", dateEpochDay = 20L)
        database.lessonNoteDao().upsert(target)
        database.lessonNoteDao().upsert(note(id = "other-date", groupName = "ISP-1", dateEpochDay = 21L))
        database.lessonNoteDao().upsert(note(id = "other-group", groupName = "ISP-2", dateEpochDay = 20L))

        assertEquals(
            listOf(target),
            database.lessonNoteDao().getByGroupAndDate("ISP-1", 20L),
        )
    }

    private fun group() = GroupEntity(
        groupName = "ISP-1",
        collegeName = "College",
        course = 1,
        courseName = "First",
        sourceUrl = "schedule.xls",
    )

    private fun lesson(id: String, groupName: String) = LessonTemplateEntity(
        id = id,
        groupName = groupName,
        dayOfWeek = 1,
        startTime = "08:30",
        endTime = "10:10",
        weekType = "ALL",
        subject = "Math",
        teacher = null,
        room = null,
        rawText = "Math",
        sourceUpdatedAtEpochMillis = 100L,
    )

    private fun note(id: String, groupName: String, dateEpochDay: Long) = LessonNoteEntity(
        id = id,
        groupName = groupName,
        dateEpochDay = dateEpochDay,
        startMinute = 510,
        endMinute = 610,
        weekType = "ALL",
        subject = "Math",
        teacher = null,
        room = null,
        rawText = "Math",
        noteText = "Read",
        reminderEnabled = false,
        reminderMinutes = null,
        remindAtEpochMillis = null,
        createdAtEpochMillis = 100L,
    )
}

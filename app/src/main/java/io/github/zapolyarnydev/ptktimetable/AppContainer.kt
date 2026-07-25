package io.github.zapolyarnydev.ptktimetable

import android.content.Context
import androidx.room.Room
import io.github.zapolyarnydev.ptktimetable.data.local.LessonNotesStore
import io.github.zapolyarnydev.ptktimetable.data.local.UserPreferencesStore
import io.github.zapolyarnydev.ptktimetable.data.local.database.AppDatabase
import io.github.zapolyarnydev.ptktimetable.data.local.database.MIGRATION_1_2
import io.github.zapolyarnydev.ptktimetable.data.local.schedule.RoomScheduleLocalDataSource
import io.github.zapolyarnydev.ptktimetable.data.mapper.NovsuLessonMapper
import io.github.zapolyarnydev.ptktimetable.data.mapper.RoomScheduleMapper
import io.github.zapolyarnydev.ptktimetable.data.normalize.LessonTextNormalizer
import io.github.zapolyarnydev.ptktimetable.data.notification.LessonReminderScheduler
import io.github.zapolyarnydev.ptktimetable.data.notification.LessonReminderWorkflow
import io.github.zapolyarnydev.ptktimetable.data.remote.NovsuScheduleRemoteDataSource
import io.github.zapolyarnydev.ptktimetable.data.remote.html.PtkCurrentWeekHtmlParser
import io.github.zapolyarnydev.ptktimetable.data.remote.html.PtkGroupsHtmlParser
import io.github.zapolyarnydev.ptktimetable.data.remote.service.PortalServiceImpl
import io.github.zapolyarnydev.ptktimetable.data.remote.xls.PtkXlsScheduleParser
import io.github.zapolyarnydev.ptktimetable.data.repository.DefaultTimetableRepository
import io.github.zapolyarnydev.ptktimetable.data.repository.PortalBackedWeekResolver
import io.github.zapolyarnydev.ptktimetable.domain.reminder.ReminderTimeCalculator
import io.github.zapolyarnydev.ptktimetable.feature.catalog.course.CourseViewModelFactory
import io.github.zapolyarnydev.ptktimetable.feature.catalog.group.GroupViewModelFactory
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleViewModelFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

class AppContainer(context: Context) {

    private val applicationContext = context.applicationContext
    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            DATABASE_NAME,
        ).addMigrations(MIGRATION_1_2).build()
    }
    private val httpClient by lazy { HttpClient(OkHttp) }
    private val portalService by lazy { PortalServiceImpl(httpClient) }
    private val sourceRepository by lazy {
        NovsuScheduleRemoteDataSource(
            portalService = portalService,
            groupsHtmlParser = PtkGroupsHtmlParser(),
            currentWeekHtmlParser = PtkCurrentWeekHtmlParser(),
            xlsScheduleParser = PtkXlsScheduleParser(),
        )
    }
    private val weekResolver by lazy { PortalBackedWeekResolver(sourceRepository) }
    private val timetableRepository by lazy {
        DefaultTimetableRepository(
            remoteDataSource = sourceRepository,
            localDataSource = RoomScheduleLocalDataSource(
                database = database,
                mapper = RoomScheduleMapper(),
            ),
            lessonMapper = NovsuLessonMapper(LessonTextNormalizer()),
        )
    }
    private val preferencesStore by lazy { UserPreferencesStore(applicationContext) }
    private val notesStore by lazy { LessonNotesStore(database.lessonNoteDao()) }
    private val reminderScheduler by lazy { LessonReminderScheduler(applicationContext) }
    private val reminderWorkflow by lazy {
        LessonReminderWorkflow(
            scheduler = reminderScheduler,
            timeCalculator = ReminderTimeCalculator(java.time.ZoneId.systemDefault()),
        )
    }

    val courseViewModelFactory by lazy {
        CourseViewModelFactory(timetableRepository)
    }

    val groupViewModelFactory by lazy {
        GroupViewModelFactory(timetableRepository)
    }

    val scheduleViewModelFactory by lazy {
        ScheduleViewModelFactory(
            timetableRepository = timetableRepository,
            weekResolver = weekResolver,
            preferencesStore = preferencesStore,
            notesStore = notesStore,
            reminderWorkflow = reminderWorkflow,
        )
    }

    private companion object {
        const val DATABASE_NAME = "ptk_timetable.db"
    }
}

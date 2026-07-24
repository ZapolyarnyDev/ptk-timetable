package io.github.zapolyarnydev.ptktimetable

import android.content.Context
import androidx.room.Room
import io.github.zapolyarnydev.ptktimetable.data.local.LessonNotesStore
import io.github.zapolyarnydev.ptktimetable.data.local.UserPreferencesStore
import io.github.zapolyarnydev.ptktimetable.data.local.database.AppDatabase
import io.github.zapolyarnydev.ptktimetable.data.mapper.NovsuLessonMapper
import io.github.zapolyarnydev.ptktimetable.data.normalize.LessonTextNormalizer
import io.github.zapolyarnydev.ptktimetable.data.notification.LessonReminderScheduler
import io.github.zapolyarnydev.ptktimetable.data.remote.NovsuScheduleRemoteDataSource
import io.github.zapolyarnydev.ptktimetable.data.remote.html.PtkCurrentWeekHtmlParser
import io.github.zapolyarnydev.ptktimetable.data.remote.html.PtkGroupsHtmlParser
import io.github.zapolyarnydev.ptktimetable.data.remote.service.PortalServiceImpl
import io.github.zapolyarnydev.ptktimetable.data.remote.xls.PtkXlsScheduleParser
import io.github.zapolyarnydev.ptktimetable.data.repository.DefaultTimetableRepository
import io.github.zapolyarnydev.ptktimetable.data.repository.PortalBackedWeekResolver
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
        ).build()
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
            lessonMapper = NovsuLessonMapper(LessonTextNormalizer()),
        )
    }
    private val preferencesStore by lazy { UserPreferencesStore(applicationContext) }
    private val notesStore by lazy { LessonNotesStore(applicationContext) }
    private val reminderScheduler by lazy { LessonReminderScheduler(applicationContext) }

    val scheduleViewModelFactory by lazy {
        database
        ScheduleViewModelFactory(
            timetableRepository = timetableRepository,
            weekResolver = weekResolver,
            preferencesStore = preferencesStore,
            notesStore = notesStore,
            reminderScheduler = reminderScheduler,
        )
    }

    private companion object {
        const val DATABASE_NAME = "ptk_timetable.db"
    }
}

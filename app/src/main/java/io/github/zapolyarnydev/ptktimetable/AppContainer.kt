package io.github.zapolyarnydev.ptktimetable

import android.content.Context
import io.github.zapolyarnydev.ptktimetable.data.local.LessonNotesStore
import io.github.zapolyarnydev.ptktimetable.data.local.UserPreferencesStore
import io.github.zapolyarnydev.ptktimetable.data.mapper.NovsuLessonMapper
import io.github.zapolyarnydev.ptktimetable.data.normalize.LessonTextNormalizer
import io.github.zapolyarnydev.ptktimetable.data.notification.LessonReminderScheduler
import io.github.zapolyarnydev.ptktimetable.data.remote.html.PtkCurrentWeekHtmlParser
import io.github.zapolyarnydev.ptktimetable.data.remote.html.PtkGroupsHtmlParser
import io.github.zapolyarnydev.ptktimetable.data.remote.service.PortalServiceImpl
import io.github.zapolyarnydev.ptktimetable.data.remote.xls.PtkXlsScheduleParser
import io.github.zapolyarnydev.ptktimetable.data.repository.DomainTimetableRepositoryAdapter
import io.github.zapolyarnydev.ptktimetable.data.repository.PortalBackedWeekResolver
import io.github.zapolyarnydev.ptktimetable.data.repository.PtkScheduleRepository
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleViewModelFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

class AppContainer(context: Context) {

    private val applicationContext = context.applicationContext
    private val httpClient by lazy { HttpClient(OkHttp) }
    private val portalService by lazy { PortalServiceImpl(httpClient) }
    private val sourceRepository by lazy {
        PtkScheduleRepository(
            portalService = portalService,
            groupsHtmlParser = PtkGroupsHtmlParser(),
            currentWeekHtmlParser = PtkCurrentWeekHtmlParser(),
            xlsScheduleParser = PtkXlsScheduleParser(),
        )
    }
    private val weekResolver by lazy { PortalBackedWeekResolver(sourceRepository) }
    private val timetableRepository by lazy {
        DomainTimetableRepositoryAdapter(
            scheduleRepository = sourceRepository,
            weekResolver = weekResolver,
            lessonMapper = NovsuLessonMapper(LessonTextNormalizer()),
        )
    }
    private val preferencesStore by lazy { UserPreferencesStore(applicationContext) }
    private val notesStore by lazy { LessonNotesStore(applicationContext) }
    private val reminderScheduler by lazy { LessonReminderScheduler(applicationContext) }

    val scheduleViewModelFactory by lazy {
        ScheduleViewModelFactory(
            timetableRepository = timetableRepository,
            weekResolver = weekResolver,
            preferencesStore = preferencesStore,
            notesStore = notesStore,
            reminderScheduler = reminderScheduler,
        )
    }
}

package io.github.zapolyarnydev.ptktimetable.ui.schedule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.zapolyarnydev.ptktimetable.data.local.LessonNote
import io.github.zapolyarnydev.ptktimetable.data.local.LessonNotesStore
import io.github.zapolyarnydev.ptktimetable.data.local.UserPreferencesStore
import io.github.zapolyarnydev.ptktimetable.data.model.PtkCurrentWeekType
import io.github.zapolyarnydev.ptktimetable.data.model.PtkGroupInfo
import io.github.zapolyarnydev.ptktimetable.data.model.PtkWeekType
import io.github.zapolyarnydev.ptktimetable.data.notification.LessonReminderScheduler
import io.github.zapolyarnydev.ptktimetable.data.repository.DomainTimetableRepositoryAdapter
import io.github.zapolyarnydev.ptktimetable.data.repository.PortalBackedWeekResolver
import io.github.zapolyarnydev.ptktimetable.data.repository.PtkScheduleRepository
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.LessonTemplate
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.TimetableGroup
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType
import io.github.zapolyarnydev.ptktimetable.domain.schedule.repository.TimetableRepository
import io.github.zapolyarnydev.ptktimetable.domain.schedule.service.WeekResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class ScheduleStep {
    COURSE_SELECTION,
    GROUP_SELECTION,
    SCHEDULE
}

enum class ScheduleMode(
    val title: String
) {
    BY_DAY("По дням"),
    BY_DATE("По дате")
}

enum class ScheduleDay(
    val title: String,
    val shortTitle: String,
    val order: Int
) {
    MONDAY("Понедельник", "Пн", 1),
    TUESDAY("Вторник", "Вт", 2),
    WEDNESDAY("Среда", "Ср", 3),
    THURSDAY("Четверг", "Чт", 4),
    FRIDAY("Пятница", "Пт", 5),
    SATURDAY("Суббота", "Сб", 6),
    SUNDAY("Воскресенье", "Вс", 7),
    UNKNOWN("Другое", "?", 99)
}

enum class ScheduleWeekFilter(
    val title: String
) {
    ALL("Обе"),
    UPPER("Верхняя"),
    LOWER("Нижняя")
}

data class CourseItem(
    val course: Int,
    val title: String
)

data class ScheduleLessonItem(
    val day: ScheduleDay,
    val dayLabel: String,
    val timeRange: String,
    val weekType: PtkWeekType,
    val subject: String,
    val teacher: String?,
    val classroom: String?,
    val rawText: String
)

data class ScheduleNoteItem(
    val noteId: String,
    val groupName: String,
    val date: LocalDate,
    val timeRange: String,
    val weekType: PtkWeekType,
    val subject: String,
    val teacher: String?,
    val classroom: String?,
    val rawText: String,
    val noteText: String,
    val reminderEnabled: Boolean,
    val reminderMinutes: Int?,
    val remindAtEpochMillis: Long?,
    val createdAtEpochMillis: Long
)

data class ScheduleUiState(
    val isLoading: Boolean = false,
    val step: ScheduleStep = ScheduleStep.COURSE_SELECTION,
    val groups: List<PtkGroupInfo> = emptyList(),
    val courses: List<CourseItem> = emptyList(),
    val selectedCourse: CourseItem? = null,
    val courseGroups: List<PtkGroupInfo> = emptyList(),
    val selectedGroup: PtkGroupInfo? = null,
    val mode: ScheduleMode = ScheduleMode.BY_DAY,
    val selectedDate: LocalDate = LocalDate.now(),
    val lessons: List<ScheduleLessonItem> = emptyList(),
    val availableDays: List<ScheduleDay> = emptyList(),
    val selectedDay: ScheduleDay? = null,
    val weekFilter: ScheduleWeekFilter = ScheduleWeekFilter.ALL,
    val currentWeekType: PtkCurrentWeekType = PtkCurrentWeekType.UNKNOWN,
    val selectedDateWeekType: PtkCurrentWeekType = PtkCurrentWeekType.UNKNOWN,
    val notes: List<ScheduleNoteItem> = emptyList(),
    val groupsUpdatedAt: Instant? = null,
    val scheduleUpdatedAt: Instant? = null,
    val errorMessage: String? = null
)

class ScheduleViewModel(
    private val timetableRepository: TimetableRepository,
    private val weekResolver: WeekResolver,
    private val preferencesStore: UserPreferencesStore,
    private val notesStore: LessonNotesStore,
    private val reminderScheduler: LessonReminderScheduler,
    private val nowProvider: () -> Instant = { Instant.now() },
    private val todayProvider: () -> LocalDate = { LocalDate.now() }
) : ViewModel() {

    private val _state = MutableStateFlow(
        ScheduleUiState(
            isLoading = true,
            selectedDate = todayProvider()
        )
    )
    val state: StateFlow<ScheduleUiState> = _state.asStateFlow()

    private var loadedTemplates: List<LessonTemplate> = emptyList()

    init {
        refreshNotes()
        loadCatalog(
            preserveCourseSelection = false,
            restoreLastSelectedGroupOnLaunch = true
        )
    }

    fun loadGroups() {
        loadCatalog(
            preserveCourseSelection = true,
            restoreLastSelectedGroupOnLaunch = false
        )
    }

    fun refreshCurrent() {
        val current = state.value
        val selectedGroup = current.selectedGroup
        if (selectedGroup != null) {
            openGroupInternal(
                group = selectedGroup,
                saveAsLastSelected = false,
                preserveUiSelection = true
            )
        } else {
            loadCatalog(
                preserveCourseSelection = true,
                restoreLastSelectedGroupOnLaunch = false
            )
        }
    }

    fun selectCourse(course: CourseItem) {
        val groups = state.value.groups
            .filter { it.course == course.course }
            .sortedBy { it.groupName }
        _state.update {
            it.copy(
                step = ScheduleStep.GROUP_SELECTION,
                selectedCourse = course,
                courseGroups = groups,
                selectedGroup = null,
                lessons = emptyList(),
                availableDays = emptyList(),
                selectedDay = null,
                errorMessage = null
            )
        }
    }

    fun openGroup(group: PtkGroupInfo) {
        openGroupInternal(group, saveAsLastSelected = true, preserveUiSelection = false)
    }

    fun backToCourses() {
        loadedTemplates = emptyList()
        _state.update {
            it.copy(
                step = ScheduleStep.COURSE_SELECTION,
                selectedCourse = null,
                courseGroups = emptyList(),
                selectedGroup = null,
                lessons = emptyList(),
                availableDays = emptyList(),
                selectedDay = null,
                errorMessage = null
            )
        }
    }

    fun backToGroups() {
        loadedTemplates = emptyList()
        _state.update {
            it.copy(
                step = ScheduleStep.GROUP_SELECTION,
                selectedGroup = null,
                lessons = emptyList(),
                availableDays = emptyList(),
                selectedDay = null,
                errorMessage = null
            )
        }
    }

    fun selectMode(mode: ScheduleMode) {
        val current = state.value
        if (current.mode == mode) return
        _state.update { it.copy(mode = mode, errorMessage = null) }
        if (mode == ScheduleMode.BY_DATE) {
            refreshDateModeLessons(current.selectedDate)
        } else {
            rebuildLessonsFromLoadedTemplates()
        }
    }

    fun selectDate(date: LocalDate) {
        val normalized = date
        _state.update {
            it.copy(
                selectedDate = normalized,
                selectedDay = dayOfWeekToScheduleDay(normalized.dayOfWeek),
                errorMessage = null
            )
        }
        if (state.value.mode == ScheduleMode.BY_DATE) {
            refreshDateModeLessons(normalized)
        }
    }

    fun previousDate() {
        selectDate(state.value.selectedDate.minusDays(1))
    }

    fun nextDate() {
        selectDate(state.value.selectedDate.plusDays(1))
    }

    fun goToToday() {
        selectDate(todayProvider())
    }

    fun selectDay(day: ScheduleDay) {
        _state.update { it.copy(selectedDay = day, errorMessage = null) }
    }

    fun nextDay() {
        shiftDay(by = 1)
    }

    fun previousDay() {
        shiftDay(by = -1)
    }

    fun selectWeekFilter(filter: ScheduleWeekFilter) {
        _state.update { it.copy(weekFilter = filter, errorMessage = null) }
    }

    fun saveNoteForLesson(
        lesson: ScheduleLessonItem,
        noteText: String
    ) {
        val current = state.value
        val group = current.selectedGroup ?: return
        if (current.mode != ScheduleMode.BY_DATE) return
        val date = current.selectedDate

        if (!canEditNote(date, lesson.timeRange)) {
            _state.update { it.copy(errorMessage = "Заметки доступны только для текущих и будущих пар") }
            return
        }

        val trimmedText = noteText.trim()
        if (trimmedText.isBlank()) {
            _state.update { it.copy(errorMessage = "Введите текст заметки") }
            return
        }

        val noteId = buildNoteId(
            groupName = group.groupName,
            date = date,
            lesson = lesson
        )

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val nowMillis = nowProvider().toEpochMilli()
                val existing = notesStore.getAll().firstOrNull { it.id == noteId }

                val note = LessonNote(
                    id = noteId,
                    groupName = group.groupName,
                    date = date,
                    timeRange = lesson.timeRange,
                    weekType = lesson.weekType.name,
                    subject = lesson.subject,
                    teacher = lesson.teacher,
                    classroom = lesson.classroom,
                    rawText = lesson.rawText,
                    noteText = trimmedText,
                    reminderEnabled = existing?.reminderEnabled == true,
                    reminderMinutes = existing?.reminderMinutes,
                    remindAtEpochMillis = existing?.remindAtEpochMillis,
                    createdAtEpochMillis = nowMillis
                )

                notesStore.upsert(note)
                if (note.reminderEnabled && note.remindAtEpochMillis != null && note.remindAtEpochMillis > nowMillis) {
                    scheduleReminder(note = note, lesson = lesson)
                } else {
                    reminderScheduler.cancel(note.id)
                }

                refreshNotesInternal()
                _state.update { it.copy(errorMessage = null) }
            }.onFailure { error ->
                _state.update { it.copy(errorMessage = error.message ?: "Не удалось сохранить заметку") }
            }
        }
    }

    fun setReminderForLesson(
        lesson: ScheduleLessonItem,
        enabled: Boolean,
        reminderMinutes: Int
    ) {
        val current = state.value
        val group = current.selectedGroup ?: return
        if (current.mode != ScheduleMode.BY_DATE) return
        val date = current.selectedDate

        if (!canEditNote(date, lesson.timeRange)) {
            _state.update { it.copy(errorMessage = "Нельзя ставить уведомление на прошедшую пару") }
            return
        }

        val noteId = buildNoteId(
            groupName = group.groupName,
            date = date,
            lesson = lesson
        )

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val nowMillis = nowProvider().toEpochMilli()
                val existing = notesStore.getAll().firstOrNull { it.id == noteId }
                val startDateTime = parseLessonStartDateTime(date, lesson.timeRange)
                val remindAtMillis = if (enabled && startDateTime != null) {
                    startDateTime
                        .minusMinutes(reminderMinutes.toLong())
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                } else {
                    null
                }

                if (enabled && (remindAtMillis == null || remindAtMillis <= nowMillis)) {
                    _state.update { it.copy(errorMessage = "Слишком поздно для уведомления, увеличьте время напоминания") }
                    return@runCatching
                }

                val updated = LessonNote(
                    id = noteId,
                    groupName = group.groupName,
                    date = date,
                    timeRange = lesson.timeRange,
                    weekType = lesson.weekType.name,
                    subject = lesson.subject,
                    teacher = lesson.teacher,
                    classroom = lesson.classroom,
                    rawText = lesson.rawText,
                    noteText = existing?.noteText.orEmpty(),
                    reminderEnabled = enabled,
                    reminderMinutes = reminderMinutes.takeIf { enabled },
                    remindAtEpochMillis = remindAtMillis?.takeIf { enabled },
                    createdAtEpochMillis = existing?.createdAtEpochMillis ?: nowMillis
                )

                notesStore.upsert(updated)

                if (enabled && updated.remindAtEpochMillis != null) {
                    scheduleReminder(note = updated, lesson = lesson)
                } else {
                    reminderScheduler.cancel(noteId)
                }

                refreshNotesInternal()
                _state.update { it.copy(errorMessage = null) }
            }.onFailure { error ->
                _state.update { it.copy(errorMessage = error.message ?: "Не удалось сохранить уведомление") }
            }
        }
    }

    fun deleteNoteForLesson(lesson: ScheduleLessonItem) {
        val current = state.value
        val group = current.selectedGroup ?: return
        if (current.mode != ScheduleMode.BY_DATE) return
        val date = current.selectedDate

        val noteId = buildNoteId(
            groupName = group.groupName,
            date = date,
            lesson = lesson
        )

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val existing = notesStore.getAll().firstOrNull { it.id == noteId }
                if (existing != null && existing.reminderEnabled) {
                    notesStore.upsert(existing.copy(noteText = ""))
                } else {
                    notesStore.remove(noteId)
                    reminderScheduler.cancel(noteId)
                }
                refreshNotesInternal()
                _state.update { it.copy(errorMessage = null) }
            }.onFailure { error ->
                _state.update { it.copy(errorMessage = error.message ?: "Не удалось удалить заметку") }
            }
        }
    }

    fun updateNoteById(
        noteId: String,
        newText: String
    ) {
        val trimmed = newText.trim()
        if (trimmed.isBlank()) {
            _state.update { it.copy(errorMessage = "Текст заметки не может быть пустым") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val all = notesStore.getAll()
                val existing = all.firstOrNull { it.id == noteId } ?: return@runCatching
                val updated = existing.copy(noteText = trimmed)
                notesStore.upsert(updated)

                if (updated.reminderEnabled && updated.remindAtEpochMillis != null && updated.remindAtEpochMillis > nowProvider().toEpochMilli()) {
                    val lesson = ScheduleLessonItem(
                        day = dayOfWeekToScheduleDay(updated.date.dayOfWeek),
                        dayLabel = dayOfWeekToScheduleDay(updated.date.dayOfWeek).title,
                        timeRange = updated.timeRange,
                        weekType = runCatching { PtkWeekType.valueOf(updated.weekType) }.getOrDefault(PtkWeekType.ALL),
                        subject = updated.subject,
                        teacher = updated.teacher,
                        classroom = updated.classroom,
                        rawText = updated.rawText
                    )
                    scheduleReminder(updated, lesson)
                }

                refreshNotesInternal()
                _state.update { it.copy(errorMessage = null) }
            }.onFailure { error ->
                _state.update { it.copy(errorMessage = error.message ?: "Не удалось обновить заметку") }
            }
        }
    }

    fun deleteNoteById(noteId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val existing = notesStore.getAll().firstOrNull { it.id == noteId }
                if (existing != null && existing.reminderEnabled) {
                    notesStore.upsert(existing.copy(noteText = ""))
                } else {
                    notesStore.remove(noteId)
                    reminderScheduler.cancel(noteId)
                }
                refreshNotesInternal()
                _state.update { it.copy(errorMessage = null) }
            }.onFailure { error ->
                _state.update { it.copy(errorMessage = error.message ?: "Не удалось удалить заметку") }
            }
        }
    }

    private fun shiftDay(by: Int) {
        val current = state.value
        val days = current.availableDays
        if (days.isEmpty()) return
        val selected = current.selectedDay ?: days.first()
        val index = days.indexOf(selected).takeIf { it >= 0 } ?: 0
        val nextIndex = (index + by).coerceIn(0, days.lastIndex)
        _state.update { it.copy(selectedDay = days[nextIndex]) }
    }

    private fun loadCatalog(
        preserveCourseSelection: Boolean,
        restoreLastSelectedGroupOnLaunch: Boolean
    ) {
        val previous = state.value
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching {
                val groups = timetableRepository.getGroups()
                    .map { it.toUiGroup() }
                    .sortedWith(compareBy<PtkGroupInfo> { it.course }.thenBy { it.groupName })
                val currentWeekType = resolveCurrentWeekType()
                Pair(groups, currentWeekType)
            }.onSuccess { (groups, currentWeekType) ->
                val courses = buildCourseItems(groups)

                val restoredGroup = if (restoreLastSelectedGroupOnLaunch) {
                    val lastSelectedGroupName = runCatching {
                        preferencesStore.getLastSelectedGroupName()
                    }.getOrNull()?.trim().orEmpty()
                    groups.firstOrNull { it.groupName == lastSelectedGroupName }
                } else {
                    null
                }

                if (restoreLastSelectedGroupOnLaunch && restoredGroup != null) {
                    val restoredCourse = courses.firstOrNull { it.course == restoredGroup.course }
                    val restoredCourseGroups = groups
                        .filter { it.course == restoredGroup.course }
                        .sortedBy { it.groupName }

                    _state.update {
                        it.copy(
                            isLoading = true,
                            step = ScheduleStep.SCHEDULE,
                            groups = groups,
                            courses = courses,
                            selectedCourse = restoredCourse,
                            courseGroups = restoredCourseGroups,
                            selectedGroup = restoredGroup,
                            lessons = emptyList(),
                            availableDays = emptyList(),
                            selectedDay = null,
                            weekFilter = defaultWeekFilter(currentWeekType),
                            currentWeekType = currentWeekType,
                            selectedDateWeekType = currentWeekType,
                            groupsUpdatedAt = nowProvider(),
                            errorMessage = null
                        )
                    }
                    openGroupInternal(
                        group = restoredGroup,
                        saveAsLastSelected = false,
                        preserveUiSelection = false
                    )
                    return@onSuccess
                }

                if (restoreLastSelectedGroupOnLaunch) {
                    runCatching { preferencesStore.setLastSelectedGroupName(null) }
                }

                val selectedCourse = if (preserveCourseSelection) {
                    val prevCourse = previous.selectedCourse?.course
                    courses.firstOrNull { it.course == prevCourse }
                } else {
                    null
                }
                val courseGroups = selectedCourse?.let { selected ->
                    groups.filter { it.course == selected.course }.sortedBy { it.groupName }
                }.orEmpty()

                _state.update {
                    it.copy(
                        isLoading = false,
                        step = if (selectedCourse != null) {
                            ScheduleStep.GROUP_SELECTION
                        } else {
                            ScheduleStep.COURSE_SELECTION
                        },
                        groups = groups,
                        courses = courses,
                        selectedCourse = selectedCourse,
                        courseGroups = courseGroups,
                        selectedGroup = null,
                        lessons = emptyList(),
                        availableDays = emptyList(),
                        selectedDay = null,
                        weekFilter = defaultWeekFilter(currentWeekType),
                        currentWeekType = currentWeekType,
                        selectedDateWeekType = currentWeekType,
                        groupsUpdatedAt = nowProvider(),
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Не удалось загрузить список групп"
                    )
                }
            }
        }
    }

    private fun openGroupInternal(
        group: PtkGroupInfo,
        saveAsLastSelected: Boolean,
        preserveUiSelection: Boolean
    ) {
        val beforeLoading = state.value
        viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(
                    isLoading = true,
                    step = ScheduleStep.SCHEDULE,
                    selectedGroup = group,
                    errorMessage = null
                )
            }

            runCatching {
                if (saveAsLastSelected) {
                    runCatching { preferencesStore.setLastSelectedGroupName(group.groupName) }
                }

                timetableRepository.getTemplatesByGroup(group.groupName)
            }.onSuccess { templates ->
                loadedTemplates = templates

                val allLessons = templates
                    .map { it.toScheduleLessonItem() }
                    .sortedBy { lessonSortKey(it.timeRange) }

                val availableDays = allLessons
                    .map { it.day }
                    .distinct()
                    .sortedBy { it.order }

                val selectedDay = resolveSelectedDay(
                    preserveUiSelection = preserveUiSelection,
                    previousSelectedDay = beforeLoading.selectedDay,
                    availableDays = availableDays
                )

                val selectedDate = if (preserveUiSelection) {
                    beforeLoading.selectedDate
                } else {
                    todayProvider()
                }

                val weekFilter = if (preserveUiSelection) {
                    beforeLoading.weekFilter
                } else {
                    defaultWeekFilter(beforeLoading.currentWeekType)
                }

                val selectedDateWeekType = if (beforeLoading.mode == ScheduleMode.BY_DATE) {
                    resolveWeekTypeForDate(selectedDate)
                } else {
                    beforeLoading.currentWeekType
                }

                val selectedDayForUi = if (beforeLoading.mode == ScheduleMode.BY_DATE) {
                    dayOfWeekToScheduleDay(selectedDate.dayOfWeek)
                } else {
                    selectedDay
                }

                val lessons = if (beforeLoading.mode == ScheduleMode.BY_DATE) {
                    buildDateLessonsFromTemplates(
                        templates = templates,
                        date = selectedDate,
                        selectedDateWeekType = selectedDateWeekType
                    )
                } else {
                    allLessons
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        step = ScheduleStep.SCHEDULE,
                        selectedGroup = group,
                        selectedDate = selectedDate,
                        lessons = lessons,
                        availableDays = availableDays,
                        selectedDay = selectedDayForUi,
                        weekFilter = weekFilter,
                        selectedDateWeekType = selectedDateWeekType,
                        scheduleUpdatedAt = nowProvider(),
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                loadedTemplates = emptyList()
                _state.update {
                    it.copy(
                        isLoading = false,
                        step = ScheduleStep.SCHEDULE,
                        selectedGroup = group,
                        lessons = emptyList(),
                        availableDays = emptyList(),
                        selectedDay = null,
                        errorMessage = error.message ?: "Не удалось загрузить расписание"
                    )
                }
            }
        }
    }

    private fun rebuildLessonsFromLoadedTemplates() {
        if (loadedTemplates.isEmpty()) return

        val current = state.value
        val allLessons = loadedTemplates
            .map { it.toScheduleLessonItem() }
            .sortedBy { lessonSortKey(it.timeRange) }

        val availableDays = allLessons
            .map { it.day }
            .distinct()
            .sortedBy { it.order }

        val selectedDay = if (current.mode == ScheduleMode.BY_DATE) {
            dayOfWeekToScheduleDay(current.selectedDate.dayOfWeek)
        } else {
            current.selectedDay?.takeIf { it in availableDays }
                ?: availableDays.firstOrNull()
        }

        val lessons = if (current.mode == ScheduleMode.BY_DATE) {
            buildDateLessonsFromTemplates(
                templates = loadedTemplates,
                date = current.selectedDate,
                selectedDateWeekType = current.selectedDateWeekType
            )
        } else {
            allLessons
        }

        _state.update {
            it.copy(
                lessons = lessons,
                availableDays = availableDays,
                selectedDay = selectedDay,
                errorMessage = null
            )
        }
    }

    private suspend fun resolveCurrentWeekType(): PtkCurrentWeekType {
        val weekInfo = runCatching { weekResolver.resolve(todayProvider()) }.getOrNull()
        return when (weekInfo?.isUpper) {
            true -> PtkCurrentWeekType.UPPER
            false -> PtkCurrentWeekType.LOWER
            null -> PtkCurrentWeekType.UNKNOWN
        }
    }

    private fun buildDateLessonsFromTemplates(
        templates: List<LessonTemplate>,
        date: LocalDate,
        selectedDateWeekType: PtkCurrentWeekType
    ): List<ScheduleLessonItem> {
        val targetDay = dayOfWeekToScheduleDay(date.dayOfWeek)
        val isUpperWeek = when (selectedDateWeekType) {
            PtkCurrentWeekType.UPPER -> true
            PtkCurrentWeekType.LOWER -> false
            PtkCurrentWeekType.UNKNOWN -> null
        }

        return templates
            .asSequence()
            .filter { it.dayOfWeek == date.dayOfWeek }
            .filter { template ->
                when (template.weekType) {
                    WeekType.ALL -> true
                    WeekType.UPPER -> isUpperWeek != false
                    WeekType.LOWER -> isUpperWeek != true
                }
            }
            .map { template -> template.toScheduleLessonItem(overrideDay = targetDay) }
            .sortedBy { lessonSortKey(it.timeRange) }
            .toList()
    }

    private fun refreshDateModeLessons(date: LocalDate) {
        if (loadedTemplates.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val provisionalLessons = buildDateLessonsFromTemplates(
                templates = loadedTemplates,
                date = date,
                selectedDateWeekType = PtkCurrentWeekType.UNKNOWN
            )
            _state.update {
                if (it.selectedDate != date || it.mode != ScheduleMode.BY_DATE) {
                    it
                } else {
                    it.copy(
                        lessons = provisionalLessons,
                        selectedDay = dayOfWeekToScheduleDay(date.dayOfWeek),
                        selectedDateWeekType = PtkCurrentWeekType.UNKNOWN,
                        errorMessage = null
                    )
                }
            }

            val selectedDateWeekType = resolveWeekTypeForDate(date)
            val lessons = buildDateLessonsFromTemplates(
                templates = loadedTemplates,
                date = date,
                selectedDateWeekType = selectedDateWeekType
            )
            _state.update {
                if (it.selectedDate != date || it.mode != ScheduleMode.BY_DATE) {
                    it
                } else {
                    it.copy(
                        lessons = lessons,
                        selectedDay = dayOfWeekToScheduleDay(date.dayOfWeek),
                        selectedDateWeekType = selectedDateWeekType,
                        errorMessage = null
                    )
                }
            }
        }
    }

    private suspend fun resolveWeekTypeForDate(date: LocalDate): PtkCurrentWeekType {
        val weekInfo = runCatching { weekResolver.resolve(date) }.getOrNull()
        return when (weekInfo?.isUpper) {
            true -> PtkCurrentWeekType.UPPER
            false -> PtkCurrentWeekType.LOWER
            null -> PtkCurrentWeekType.UNKNOWN
        }
    }

    private fun refreshNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            refreshNotesInternal()
        }
    }

    private suspend fun refreshNotesInternal() {
        runCatching {
            notesStore.getAll()
                .sortedWith(compareBy<LessonNote> { it.date }.thenBy { lessonSortKey(it.timeRange) }.thenBy { it.createdAtEpochMillis })
                .mapNotNull { it.toUiNote() }
        }.onSuccess { notes ->
            _state.update { current -> current.copy(notes = notes) }
        }.onFailure { error ->
            _state.update { current ->
                current.copy(
                    notes = emptyList(),
                    errorMessage = error.message ?: "Не удалось загрузить заметки"
                )
            }
        }
    }

    private fun LessonNote.toUiNote(): ScheduleNoteItem? {
        val weekType = runCatching { PtkWeekType.valueOf(weekType) }.getOrNull() ?: return null
        return ScheduleNoteItem(
            noteId = id,
            groupName = groupName,
            date = date,
            timeRange = timeRange,
            weekType = weekType,
            subject = subject,
            teacher = teacher,
            classroom = classroom,
            rawText = rawText,
            noteText = noteText,
            reminderEnabled = reminderEnabled,
            reminderMinutes = reminderMinutes,
            remindAtEpochMillis = remindAtEpochMillis,
            createdAtEpochMillis = createdAtEpochMillis
        )
    }

    private fun buildNoteId(
        groupName: String,
        date: LocalDate,
        lesson: ScheduleLessonItem
    ): String {
        return LessonNotesStore.buildLessonNoteId(
            groupName = groupName,
            date = date,
            timeRange = lesson.timeRange,
            weekType = lesson.weekType.name,
            rawText = lesson.rawText
        )
    }

    private fun canEditNote(
        date: LocalDate,
        timeRange: String
    ): Boolean {
        val now = nowProvider().atZone(ZoneId.systemDefault()).toLocalDateTime()
        val startDateTime = parseLessonStartDateTime(date, timeRange) ?: return false
        return !startDateTime.isBefore(now)
    }

    private fun parseLessonStartDateTime(
        date: LocalDate,
        timeRange: String
    ): LocalDateTime? {
        val startTime = LessonNotesStore.parseStartTimeOrNull(timeRange) ?: return null
        return LocalDateTime.of(date, startTime)
    }

    private fun scheduleReminder(
        note: LessonNote,
        lesson: ScheduleLessonItem
    ) {
        val trigger = note.remindAtEpochMillis ?: return
        reminderScheduler.schedule(
            noteId = note.id,
            triggerAtMillis = trigger,
            title = "Скоро пара: ${lesson.subject.ifBlank { "занятие" }}",
            message = buildReminderMessage(note.groupName, note.date, lesson.timeRange, note.noteText)
        )
    }

    private fun buildReminderMessage(
        groupName: String,
        date: LocalDate,
        timeRange: String,
        noteText: String?
    ): String {
        val base = "Группа $groupName, $timeRange, ${date.format(DATE_TITLE_FORMATTER)}"
        val note = noteText?.trim().orEmpty()
        return if (note.isNotBlank()) "$base\nЗаметка: $note" else base
    }

    private fun buildCourseItems(groups: List<PtkGroupInfo>): List<CourseItem> {
        return groups
            .groupBy { it.course }
            .map { (course, items) ->
                val title = items.firstOrNull()?.courseName?.takeIf { it.isNotBlank() } ?: "$course курс"
                CourseItem(course = course, title = title)
            }
            .sortedBy { it.course }
    }

    private fun resolveSelectedDay(
        preserveUiSelection: Boolean,
        previousSelectedDay: ScheduleDay?,
        availableDays: List<ScheduleDay>
    ): ScheduleDay? {
        if (availableDays.isEmpty()) return null
        if (preserveUiSelection && previousSelectedDay != null && previousSelectedDay in availableDays) {
            return previousSelectedDay
        }

        val today = dayOfWeekToScheduleDay(todayProvider().dayOfWeek)
        return availableDays.firstOrNull { it == today } ?: availableDays.first()
    }

    private fun dayOfWeekToScheduleDay(dayOfWeek: DayOfWeek): ScheduleDay {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> ScheduleDay.MONDAY
            DayOfWeek.TUESDAY -> ScheduleDay.TUESDAY
            DayOfWeek.WEDNESDAY -> ScheduleDay.WEDNESDAY
            DayOfWeek.THURSDAY -> ScheduleDay.THURSDAY
            DayOfWeek.FRIDAY -> ScheduleDay.FRIDAY
            DayOfWeek.SATURDAY -> ScheduleDay.SATURDAY
            DayOfWeek.SUNDAY -> ScheduleDay.SUNDAY
        }
    }

    private fun defaultWeekFilter(currentWeekType: PtkCurrentWeekType): ScheduleWeekFilter {
        return when (currentWeekType) {
            PtkCurrentWeekType.UPPER -> ScheduleWeekFilter.UPPER
            PtkCurrentWeekType.LOWER -> ScheduleWeekFilter.LOWER
            PtkCurrentWeekType.UNKNOWN -> ScheduleWeekFilter.ALL
        }
    }

    private fun lessonSortKey(timeRange: String): Int {
        val normalized = timeRange.replace('—', '-').replace('–', '-')
        val match = Regex("(\\d{1,2})[.:](\\d{2})").find(normalized) ?: return Int.MAX_VALUE
        val hours = match.groupValues[1].toIntOrNull() ?: return Int.MAX_VALUE
        val minutes = match.groupValues[2].toIntOrNull() ?: return Int.MAX_VALUE
        return hours * 60 + minutes
    }

    private fun TimetableGroup.toUiGroup(): PtkGroupInfo {
        return PtkGroupInfo(
            collegeName = collegeName,
            course = course,
            courseName = courseName,
            groupName = groupName,
            xlsUrl = sourceUrl
        )
    }

    private fun LessonTemplate.toScheduleLessonItem(
        overrideDay: ScheduleDay = dayOfWeekToScheduleDay(dayOfWeek)
    ): ScheduleLessonItem {
        return ScheduleLessonItem(
            day = overrideDay,
            dayLabel = overrideDay.title,
            timeRange = formatTimeRange(startTime, endTime),
            weekType = weekType.toUiWeekType(),
            subject = subject,
            teacher = teacher,
            classroom = room,
            rawText = rawText
        )
    }

    private fun WeekType.toUiWeekType(): PtkWeekType {
        return when (this) {
            WeekType.ALL -> PtkWeekType.ALL
            WeekType.UPPER -> PtkWeekType.UPPER
            WeekType.LOWER -> PtkWeekType.LOWER
        }
    }

    private fun formatTimeRange(start: LocalTime, end: LocalTime): String {
        return "${TIME_FORMATTER.format(start)}-${TIME_FORMATTER.format(end)}"
    }

    private companion object {
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("H.mm")
        val DATE_TITLE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }
}

class ScheduleViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            val baseRepository = PtkScheduleRepository()
            val weekResolver = PortalBackedWeekResolver(baseRepository)
            val timetableRepository = DomainTimetableRepositoryAdapter(
                scheduleRepository = baseRepository,
                weekResolver = weekResolver
            )

            @Suppress("UNCHECKED_CAST")
            return ScheduleViewModel(
                timetableRepository = timetableRepository,
                weekResolver = weekResolver,
                preferencesStore = UserPreferencesStore(context.applicationContext),
                notesStore = LessonNotesStore(context.applicationContext),
                reminderScheduler = LessonReminderScheduler(context.applicationContext)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

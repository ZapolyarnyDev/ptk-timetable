package io.github.zapolyarnydev.ptktimetable.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.zapolyarnydev.ptktimetable.data.local.LessonNote
import io.github.zapolyarnydev.ptktimetable.data.local.LessonNotesStore
import io.github.zapolyarnydev.ptktimetable.data.local.UserPreferencesStore
import io.github.zapolyarnydev.ptktimetable.data.notification.LessonReminderWorkflow
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.CachedData
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Group
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Lesson
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.ScheduleMode
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekFilter
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType
import io.github.zapolyarnydev.ptktimetable.domain.schedule.repository.TimetableRepository
import io.github.zapolyarnydev.ptktimetable.domain.schedule.service.WeekResolver
import io.github.zapolyarnydev.ptktimetable.domain.schedule.service.WeekRules
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class ScheduleDay(val title: String, val shortTitle: String, val order: Int) {
    MONDAY("Понедельник", "Пн", 1),
    TUESDAY("Вторник", "Вт", 2),
    WEDNESDAY("Среда", "Ср", 3),
    THURSDAY("Четверг", "Чт", 4),
    FRIDAY("Пятница", "Пт", 5),
    SATURDAY("Суббота", "Сб", 6),
    SUNDAY("Воскресенье", "Вс", 7),
    UNKNOWN("Другое", "?", 99),
}

data class ScheduleLessonItem(
    val day: ScheduleDay,
    val dayLabel: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val weekType: WeekType,
    val subject: String,
    val teacher: String?,
    val classroom: String?,
    val rawText: String,
) {
    val timeRange: String get() = "${TIME_FORMATTER.format(startTime)}-${TIME_FORMATTER.format(endTime)}"

    private companion object {
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("H.mm")
    }
}

data class ScheduleNoteItem(
    val noteId: String,
    val groupName: String,
    val date: LocalDate,
    val timeRange: String,
    val weekType: WeekType,
    val subject: String,
    val teacher: String?,
    val classroom: String?,
    val rawText: String,
    val noteText: String,
    val reminderEnabled: Boolean,
    val reminderMinutes: Int?,
    val remindAtEpochMillis: Long?,
    val createdAtEpochMillis: Long,
)

data class ScheduleUiState(
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val syncError: String? = null,
    val hasCachedData: Boolean = false,
    val isOffline: Boolean = false,
    val selectedGroup: Group? = null,
    val mode: ScheduleMode = ScheduleMode.BY_DAY,
    val selectedDate: LocalDate = LocalDate.now(),
    val lessons: List<ScheduleLessonItem> = emptyList(),
    val availableDays: List<ScheduleDay> = emptyList(),
    val selectedDay: ScheduleDay? = null,
    val weekFilter: WeekFilter = WeekFilter.ALL,
    val currentWeekType: WeekType? = null,
    val selectedDateWeekType: WeekType? = null,
    val notes: List<ScheduleNoteItem> = emptyList(),
    val scheduleUpdatedAt: Instant? = null,
    val errorMessage: String? = null,
)

class ScheduleViewModel(
    private val timetableRepository: TimetableRepository,
    private val weekResolver: WeekResolver,
    private val preferencesStore: UserPreferencesStore,
    private val notesStore: LessonNotesStore,
    private val reminderWorkflow: LessonReminderWorkflow,
    private val nowProvider: () -> Instant = { Instant.now() },
    private val todayProvider: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    private val _state = MutableStateFlow(
        ScheduleUiState(
            isInitialLoading = true,
            selectedDate = todayProvider(),
        ),
    )
    val state: StateFlow<ScheduleUiState> = _state.asStateFlow()

    private var loadedTemplates: List<Lesson> = emptyList()
    private var lessonsJob: Job? = null

    init {
        refreshNotes()
    }

    fun refreshCurrent() {
        state.value.selectedGroup?.let {
            openGroupInternal(it, saveAsLastSelected = false, preserveUiSelection = true)
        }
    }

    fun openGroup(groupId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val group = timetableRepository.observeGroups().first().data
                .firstOrNull { it.groupName.equals(groupId, ignoreCase = true) }
            if (group == null) {
                _state.update {
                    it.copy(
                        isInitialLoading = false,
                        syncError = "Группа $groupId не найдена в кеше",
                    )
                }
                return@launch
            }
            openGroupInternal(group, saveAsLastSelected = true, preserveUiSelection = false)
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
                errorMessage = null,
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

    fun selectWeekFilter(filter: WeekFilter) {
        _state.update { it.copy(weekFilter = filter, errorMessage = null) }
    }

    fun saveNoteForLesson(lesson: ScheduleLessonItem, noteText: String) {
        val current = state.value
        val group = current.selectedGroup ?: return
        if (current.mode != ScheduleMode.BY_DATE) return
        val date = current.selectedDate

        if (!canEditNote(date, lesson.startTime)) {
            _state.update { it.copy(errorMessage = "Заметки доступны только для текущих и будущих пар") }
            return
        }

        val trimmedText = noteText.trim()
        if (trimmedText.isBlank()) {
            _state.update { it.copy(errorMessage = "Введите текст заметки") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val nowMillis = nowProvider().toEpochMilli()
                val existing = findNoteForLesson(group.groupName, date, lesson)

                val note = LessonNote(
                    id = existing?.id ?: notesStore.newId(),
                    groupName = group.groupName,
                    date = date,
                    startTime = lesson.startTime,
                    endTime = lesson.endTime,
                    weekType = lesson.weekType,
                    subject = lesson.subject,
                    teacher = lesson.teacher,
                    classroom = lesson.classroom,
                    rawText = lesson.rawText,
                    noteText = trimmedText,
                    reminderId = existing?.reminderId,
                    reminderEnabled = existing?.reminderEnabled == true,
                    reminderMinutes = existing?.reminderMinutes,
                    remindAtEpochMillis = existing?.remindAtEpochMillis,
                    createdAtEpochMillis = existing?.createdAtEpochMillis ?: nowMillis,
                )

                notesStore.upsert(reminderWorkflow.reschedule(note))

                refreshNotesInternal()
                _state.update { it.copy(errorMessage = null) }
            }.onFailure { error ->
                _state.update { it.copy(errorMessage = error.message ?: "Не удалось сохранить заметку") }
            }
        }
    }

    fun setReminderForLesson(lesson: ScheduleLessonItem, enabled: Boolean, reminderMinutes: Int) {
        val current = state.value
        val group = current.selectedGroup ?: return
        if (current.mode != ScheduleMode.BY_DATE) return
        val date = current.selectedDate

        if (!canEditNote(date, lesson.startTime)) {
            _state.update { it.copy(errorMessage = "Нельзя ставить уведомление на прошедшую пару") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val nowMillis = nowProvider().toEpochMilli()
                val existing = findNoteForLesson(group.groupName, date, lesson)
                val baseNote = LessonNote(
                    id = existing?.id ?: notesStore.newId(),
                    groupName = group.groupName,
                    date = date,
                    startTime = lesson.startTime,
                    endTime = lesson.endTime,
                    weekType = lesson.weekType,
                    subject = lesson.subject,
                    teacher = lesson.teacher,
                    classroom = lesson.classroom,
                    rawText = lesson.rawText,
                    noteText = existing?.noteText.orEmpty(),
                    reminderId = existing?.reminderId,
                    reminderEnabled = existing?.reminderEnabled == true,
                    reminderMinutes = existing?.reminderMinutes,
                    remindAtEpochMillis = existing?.remindAtEpochMillis,
                    createdAtEpochMillis = existing?.createdAtEpochMillis ?: nowMillis,
                )
                val updated = if (!enabled) {
                    reminderWorkflow.cancel(baseNote)
                } else if (existing?.reminderEnabled == true) {
                    reminderWorkflow.change(baseNote, reminderMinutes)
                } else {
                    reminderWorkflow.create(baseNote, reminderMinutes)
                }
                notesStore.upsert(updated)

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

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val existing = findNoteForLesson(group.groupName, date, lesson)
                if (existing != null && existing.reminderEnabled) {
                    notesStore.upsert(reminderWorkflow.reschedule(existing.copy(noteText = "")))
                } else if (existing != null) {
                    notesStore.remove(existing.id)
                }
                refreshNotesInternal()
                _state.update { it.copy(errorMessage = null) }
            }.onFailure { error ->
                _state.update { it.copy(errorMessage = error.message ?: "Не удалось удалить заметку") }
            }
        }
    }

    fun updateNoteById(noteId: String, newText: String) {
        val trimmed = newText.trim()
        if (trimmed.isBlank()) {
            _state.update { it.copy(errorMessage = "Текст заметки не может быть пустым") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val existing = notesStore.getById(noteId) ?: return@runCatching
                val updated = existing.copy(noteText = trimmed)
                notesStore.upsert(reminderWorkflow.reschedule(updated))

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
                val existing = notesStore.getById(noteId)
                if (existing != null && existing.reminderEnabled) {
                    notesStore.upsert(reminderWorkflow.reschedule(existing.copy(noteText = "")))
                } else {
                    notesStore.remove(noteId)
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

    private fun openGroupInternal(group: Group, saveAsLastSelected: Boolean, preserveUiSelection: Boolean) {
        val beforeLoading = state.value
        val sameGroup = beforeLoading.selectedGroup?.groupName.equals(group.groupName, ignoreCase = true)
        lessonsJob?.cancel()
        lessonsJob = viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                val hasCache = sameGroup && it.lessons.isNotEmpty()
                it.copy(
                    isInitialLoading = !hasCache,
                    isRefreshing = hasCache,
                    syncError = null,
                    hasCachedData = hasCache,
                    isOffline = false,
                    selectedGroup = group,
                    lessons = if (sameGroup) it.lessons else emptyList(),
                    availableDays = if (sameGroup) it.availableDays else emptyList(),
                    selectedDay = if (sameGroup) it.selectedDay else null,
                    errorMessage = null,
                )
            }

            try {
                if (saveAsLastSelected) {
                    runCatching { preferencesStore.setLastSelectedGroupName(group.groupName) }
                }

                val cached = timetableRepository.observeLessons(group.groupName).first()
                applyLessonSnapshot(
                    snapshot = cached,
                    group = group,
                    beforeLoading = beforeLoading,
                    preserveUiSelection = preserveUiSelection,
                    isRefreshInProgress = true,
                )
                val currentWeekType = resolveCurrentWeekType()
                _state.update {
                    it.copy(
                        currentWeekType = currentWeekType,
                        selectedDateWeekType = if (it.mode == ScheduleMode.BY_DATE) {
                            it.selectedDateWeekType
                        } else {
                            currentWeekType
                        },
                        weekFilter = if (preserveUiSelection) {
                            it.weekFilter
                        } else {
                            defaultWeekFilter(currentWeekType)
                        },
                    )
                }

                timetableRepository.refreshLessons(group)
                val refreshed = timetableRepository.observeLessons(group.groupName).first()
                applyLessonSnapshot(
                    snapshot = refreshed,
                    group = group,
                    beforeLoading = beforeLoading.copy(
                        currentWeekType = currentWeekType,
                        selectedDateWeekType = currentWeekType,
                    ),
                    preserveUiSelection = preserveUiSelection,
                    isRefreshInProgress = false,
                )
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                _state.update {
                    if (!it.selectedGroup?.groupName.equals(group.groupName, ignoreCase = true)) {
                        it
                    } else {
                        val hasCache = it.lessons.isNotEmpty()
                        it.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            syncError = error.message ?: "Не удалось обновить расписание",
                            hasCachedData = hasCache,
                            isOffline = hasCache,
                        )
                    }
                }
            }
        }
    }

    private suspend fun applyLessonSnapshot(
        snapshot: CachedData<List<Lesson>>,
        group: Group,
        beforeLoading: ScheduleUiState,
        preserveUiSelection: Boolean,
        isRefreshInProgress: Boolean,
    ) {
        if (!state.value.selectedGroup?.groupName.equals(group.groupName, ignoreCase = true)) return

        loadedTemplates = snapshot.data
        val allLessons = snapshot.data
            .map { it.toScheduleLessonItem() }
            .sortedBy { it.startTime }
        val availableDays = allLessons
            .map { it.day }
            .distinct()
            .sortedBy { it.order }
        val selectedDay = resolveSelectedDay(
            preserveUiSelection = preserveUiSelection,
            previousSelectedDay = beforeLoading.selectedDay,
            availableDays = availableDays,
        )
        val selectedDate = if (preserveUiSelection) beforeLoading.selectedDate else todayProvider()
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
                templates = snapshot.data,
                date = selectedDate,
                selectedDateWeekType = selectedDateWeekType,
            )
        } else {
            allLessons
        }
        val hasCache = snapshot.data.isNotEmpty()

        _state.update {
            if (!it.selectedGroup?.groupName.equals(group.groupName, ignoreCase = true)) {
                it
            } else {
                it.copy(
                    isInitialLoading = isRefreshInProgress && !hasCache,
                    isRefreshing = isRefreshInProgress && hasCache,
                    syncError = null,
                    hasCachedData = hasCache,
                    isOffline = false,
                    selectedGroup = group,
                    selectedDate = selectedDate,
                    lessons = lessons,
                    availableDays = availableDays,
                    selectedDay = selectedDayForUi,
                    weekFilter = weekFilter,
                    selectedDateWeekType = selectedDateWeekType,
                    scheduleUpdatedAt = snapshot.updatedAt,
                    errorMessage = null,
                )
            }
        }
    }

    private fun rebuildLessonsFromLoadedTemplates() {
        if (loadedTemplates.isEmpty()) return

        val current = state.value
        val allLessons = loadedTemplates
            .map { it.toScheduleLessonItem() }
            .sortedBy { it.startTime }

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
                selectedDateWeekType = current.selectedDateWeekType,
            )
        } else {
            allLessons
        }

        _state.update {
            it.copy(
                lessons = lessons,
                availableDays = availableDays,
                selectedDay = selectedDay,
                errorMessage = null,
            )
        }
    }

    private suspend fun resolveCurrentWeekType(): WeekType? {
        val weekInfo = runCatching { weekResolver.resolve(todayProvider()) }.getOrNull()
        return when (weekInfo?.isUpper) {
            true -> WeekType.UPPER
            false -> WeekType.LOWER
            null -> null
        }
    }

    private fun buildDateLessonsFromTemplates(
        templates: List<Lesson>,
        date: LocalDate,
        selectedDateWeekType: WeekType?,
    ): List<ScheduleLessonItem> {
        val targetDay = dayOfWeekToScheduleDay(date.dayOfWeek)

        return templates
            .asSequence()
            .filter { it.dayOfWeek == date.dayOfWeek }
            .filter { template -> WeekRules.matches(template.weekType, selectedDateWeekType) }
            .map { template -> template.toScheduleLessonItem(overrideDay = targetDay) }
            .sortedBy { it.startTime }
            .toList()
    }

    private fun refreshDateModeLessons(date: LocalDate) {
        if (loadedTemplates.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val provisionalLessons = buildDateLessonsFromTemplates(
                templates = loadedTemplates,
                date = date,
                selectedDateWeekType = null,
            )
            _state.update {
                if (it.selectedDate != date || it.mode != ScheduleMode.BY_DATE) {
                    it
                } else {
                    it.copy(
                        lessons = provisionalLessons,
                        selectedDay = dayOfWeekToScheduleDay(date.dayOfWeek),
                        selectedDateWeekType = null,
                        errorMessage = null,
                    )
                }
            }

            val selectedDateWeekType = resolveWeekTypeForDate(date)
            val lessons = buildDateLessonsFromTemplates(
                templates = loadedTemplates,
                date = date,
                selectedDateWeekType = selectedDateWeekType,
            )
            _state.update {
                if (it.selectedDate != date || it.mode != ScheduleMode.BY_DATE) {
                    it
                } else {
                    it.copy(
                        lessons = lessons,
                        selectedDay = dayOfWeekToScheduleDay(date.dayOfWeek),
                        selectedDateWeekType = selectedDateWeekType,
                        errorMessage = null,
                    )
                }
            }
        }
    }

    private suspend fun resolveWeekTypeForDate(date: LocalDate): WeekType? {
        val weekInfo = runCatching { weekResolver.resolve(date) }.getOrNull()
        return when (weekInfo?.isUpper) {
            true -> WeekType.UPPER
            false -> WeekType.LOWER
            null -> null
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
                .sortedWith(
                    compareBy<LessonNote> {
                        it.date
                    }.thenBy { it.startTime }.thenBy { it.createdAtEpochMillis },
                )
                .mapNotNull { it.toUiNote() }
        }.onSuccess { notes ->
            _state.update { current -> current.copy(notes = notes) }
        }.onFailure { error ->
            _state.update { current ->
                current.copy(
                    notes = emptyList(),
                    errorMessage = error.message ?: "Не удалось загрузить заметки",
                )
            }
        }
    }

    private fun LessonNote.toUiNote(): ScheduleNoteItem? = ScheduleNoteItem(
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
        createdAtEpochMillis = createdAtEpochMillis,
    )

    private suspend fun findNoteForLesson(
        groupName: String,
        date: LocalDate,
        lesson: ScheduleLessonItem,
    ): LessonNote? = notesStore.findForLesson(
        groupName = groupName,
        date = date,
        startTime = lesson.startTime,
        endTime = lesson.endTime,
        weekType = lesson.weekType,
        rawText = lesson.rawText,
    )

    private fun canEditNote(date: LocalDate, startTime: LocalTime): Boolean {
        val now = nowProvider().atZone(ZoneId.systemDefault()).toLocalDateTime()
        val startDateTime = parseLessonStartDateTime(date, startTime)
        return !startDateTime.isBefore(now)
    }

    private fun parseLessonStartDateTime(date: LocalDate, startTime: LocalTime): LocalDateTime =
        LocalDateTime.of(date, startTime)

    private fun resolveSelectedDay(
        preserveUiSelection: Boolean,
        previousSelectedDay: ScheduleDay?,
        availableDays: List<ScheduleDay>,
    ): ScheduleDay? {
        if (availableDays.isEmpty()) return null
        if (preserveUiSelection && previousSelectedDay != null && previousSelectedDay in availableDays) {
            return previousSelectedDay
        }

        val today = dayOfWeekToScheduleDay(todayProvider().dayOfWeek)
        return availableDays.firstOrNull { it == today } ?: availableDays.first()
    }

    private fun dayOfWeekToScheduleDay(dayOfWeek: DayOfWeek): ScheduleDay = when (dayOfWeek) {
        DayOfWeek.MONDAY -> ScheduleDay.MONDAY
        DayOfWeek.TUESDAY -> ScheduleDay.TUESDAY
        DayOfWeek.WEDNESDAY -> ScheduleDay.WEDNESDAY
        DayOfWeek.THURSDAY -> ScheduleDay.THURSDAY
        DayOfWeek.FRIDAY -> ScheduleDay.FRIDAY
        DayOfWeek.SATURDAY -> ScheduleDay.SATURDAY
        DayOfWeek.SUNDAY -> ScheduleDay.SUNDAY
    }

    private fun defaultWeekFilter(currentWeekType: WeekType?): WeekFilter = when (currentWeekType) {
        WeekType.UPPER -> WeekFilter.UPPER
        WeekType.LOWER -> WeekFilter.LOWER
        WeekType.ALL, null -> WeekFilter.ALL
    }

    private fun Lesson.toScheduleLessonItem(
        overrideDay: ScheduleDay = dayOfWeekToScheduleDay(dayOfWeek),
    ): ScheduleLessonItem = ScheduleLessonItem(
        day = overrideDay,
        dayLabel = overrideDay.title,
        startTime = startTime,
        endTime = endTime,
        weekType = weekType,
        subject = subject,
        teacher = teacher,
        classroom = room,
        rawText = rawText,
    )
}

class ScheduleViewModelFactory(
    private val timetableRepository: TimetableRepository,
    private val weekResolver: WeekResolver,
    private val preferencesStore: UserPreferencesStore,
    private val notesStore: LessonNotesStore,
    private val reminderWorkflow: LessonReminderWorkflow,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleViewModel(
                timetableRepository = timetableRepository,
                weekResolver = weekResolver,
                preferencesStore = preferencesStore,
                notesStore = notesStore,
                reminderWorkflow = reminderWorkflow,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

package io.github.zapolyarnydev.ptktimetable.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.zapolyarnydev.ptktimetable.data.local.UserPreferencesStore
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class ScheduleViewModel(
    private val timetableRepository: TimetableRepository,
    private val weekResolver: WeekResolver,
    private val preferencesStore: UserPreferencesStore,
    private val nowProvider: () -> Instant = { Instant.now() },
    private val todayProvider: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    private val _state = MutableStateFlow(
        ScheduleUiState(
            isInitialLoading = true,
            selectedDate = todayProvider(),
        ),
    )
    val state: StateFlow<ScheduleUiState> = _state
        .map(::buildPresentation)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = buildPresentation(_state.value),
        )

    private val _events = Channel<ScheduleUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var loadedTemplates: List<Lesson> = emptyList()
    private var lessonsJob: Job? = null

    fun onAction(action: ScheduleUiAction) {
        when (action) {
            ScheduleUiAction.Refresh -> refreshCurrent()

            ScheduleUiAction.Back -> viewModelScope.launch {
                _events.send(ScheduleUiEvent.NavigateBack)
            }

            is ScheduleUiAction.SelectMode -> selectMode(action.mode)

            is ScheduleUiAction.SelectDay -> selectDay(action.day)

            ScheduleUiAction.PreviousDay -> previousDay()

            ScheduleUiAction.NextDay -> nextDay()

            is ScheduleUiAction.SelectDate -> selectDate(action.date)

            ScheduleUiAction.PreviousDate -> previousDate()

            ScheduleUiAction.NextDate -> nextDate()

            ScheduleUiAction.Today -> goToToday()

            is ScheduleUiAction.SelectWeekFilter -> selectWeekFilter(action.filter)
        }
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

    private fun buildPresentation(raw: ScheduleUiState): ScheduleUiState {
        val visibleLessons = ScheduleRules.visibleLessons(raw)
        val now = LocalDateTime.ofInstant(nowProvider(), ZoneId.systemDefault())

        return raw.copy(
            presentation = ScheduleDataPresentation(
                visibleLessons = visibleLessons,
                timeSlots = buildTimeSlots(visibleLessons),
                currentLesson = ScheduleRules.currentLesson(
                    lessons = visibleLessons,
                    date = raw.selectedDate,
                    selectedDay = raw.selectedDay,
                    isDateMode = raw.mode == ScheduleMode.BY_DATE,
                    now = now,
                ),
                nextLesson = ScheduleRules.nextLesson(
                    lessons = visibleLessons,
                    date = raw.selectedDate,
                    selectedDay = raw.selectedDay,
                    isDateMode = raw.mode == ScheduleMode.BY_DATE,
                    now = now,
                ),
            ),
        )
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
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleViewModel(
                timetableRepository = timetableRepository,
                weekResolver = weekResolver,
                preferencesStore = preferencesStore,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

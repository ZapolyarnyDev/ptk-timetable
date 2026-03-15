package io.github.zapolyarnydev.ptktimetable.ui.schedule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.zapolyarnydev.ptktimetable.data.local.UserPreferencesStore
import io.github.zapolyarnydev.ptktimetable.data.model.PtkCurrentWeekType
import io.github.zapolyarnydev.ptktimetable.data.model.PtkGroupInfo
import io.github.zapolyarnydev.ptktimetable.data.model.PtkWeekType
import io.github.zapolyarnydev.ptktimetable.data.normalize.LessonTextNormalizer
import io.github.zapolyarnydev.ptktimetable.data.repository.PtkScheduleRepository
import io.github.zapolyarnydev.ptktimetable.data.repository.ScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.util.Locale

enum class ScheduleStep {
    COURSE_SELECTION,
    GROUP_SELECTION,
    SCHEDULE
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

data class ScheduleUiState(
    val isLoading: Boolean = false,
    val step: ScheduleStep = ScheduleStep.COURSE_SELECTION,
    val groups: List<PtkGroupInfo> = emptyList(),
    val courses: List<CourseItem> = emptyList(),
    val selectedCourse: CourseItem? = null,
    val courseGroups: List<PtkGroupInfo> = emptyList(),
    val selectedGroup: PtkGroupInfo? = null,
    val lessons: List<ScheduleLessonItem> = emptyList(),
    val availableDays: List<ScheduleDay> = emptyList(),
    val selectedDay: ScheduleDay? = null,
    val weekFilter: ScheduleWeekFilter = ScheduleWeekFilter.ALL,
    val currentWeekType: PtkCurrentWeekType = PtkCurrentWeekType.UNKNOWN,
    val groupsUpdatedAt: Instant? = null,
    val scheduleUpdatedAt: Instant? = null,
    val errorMessage: String? = null
)

class ScheduleViewModel(
    private val repository: ScheduleRepository,
    private val preferencesStore: UserPreferencesStore,
    private val lessonTextNormalizer: LessonTextNormalizer = LessonTextNormalizer(),
    private val nowProvider: () -> Instant = { Instant.now() },
    private val todayProvider: () -> LocalDate = { LocalDate.now() }
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduleUiState(isLoading = true))
    val state: StateFlow<ScheduleUiState> = _state.asStateFlow()

    init {
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
                val groups = repository.getGroups()
                    .sortedWith(compareBy<PtkGroupInfo> { it.course }.thenBy { it.groupName })
                val currentWeekType = repository.getCurrentWeekType()
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

                repository.getScheduleForGroup(group.groupName)
                    .map { raw ->
                        val day = parseDay(raw.dayOfWeek)
                        val normalized = lessonTextNormalizer.normalize(raw.rawText)
                        ScheduleLessonItem(
                            day = day,
                            dayLabel = day.title,
                            timeRange = raw.timeRange,
                            weekType = raw.weekType,
                            subject = normalized.subject,
                            teacher = normalized.teacher,
                            classroom = normalized.classroom,
                            rawText = raw.rawText
                        )
                    }
            }.onSuccess { lessons ->
                val availableDays = lessons.map { it.day }
                    .distinct()
                    .sortedBy { it.order }

                val selectedDay = resolveSelectedDay(
                    preserveUiSelection = preserveUiSelection,
                    previousSelectedDay = beforeLoading.selectedDay,
                    availableDays = availableDays
                )
                val weekFilter = if (preserveUiSelection) {
                    beforeLoading.weekFilter
                } else {
                    defaultWeekFilter(beforeLoading.currentWeekType)
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        step = ScheduleStep.SCHEDULE,
                        selectedGroup = group,
                        lessons = lessons,
                        availableDays = availableDays,
                        selectedDay = selectedDay,
                        weekFilter = weekFilter,
                        scheduleUpdatedAt = nowProvider(),
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
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

    private fun buildCourseItems(groups: List<PtkGroupInfo>): List<CourseItem> {
        return groups
            .groupBy { it.course }
            .map { (course, items) ->
                val title = items.firstOrNull()?.courseName?.takeIf { it.isNotBlank() } ?: "$course курс"
                CourseItem(course = course, title = title)
            }
            .sortedBy { it.course }
    }

    private fun parseDay(rawDay: String): ScheduleDay {
        val normalized = rawDay.lowercase(Locale.ROOT).replace('ё', 'е').trim()
        return when {
            normalized.contains("понедельник") || normalized == "пн" -> ScheduleDay.MONDAY
            normalized.contains("вторник") || normalized == "вт" -> ScheduleDay.TUESDAY
            normalized.contains("среда") || normalized == "ср" -> ScheduleDay.WEDNESDAY
            normalized.contains("четверг") || normalized == "чт" -> ScheduleDay.THURSDAY
            normalized.contains("пятница") || normalized == "пт" -> ScheduleDay.FRIDAY
            normalized.contains("суббота") || normalized == "сб" -> ScheduleDay.SATURDAY
            normalized.contains("воскресенье") || normalized == "вс" -> ScheduleDay.SUNDAY
            else -> ScheduleDay.UNKNOWN
        }
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

        val today = when (todayProvider().dayOfWeek) {
            DayOfWeek.MONDAY -> ScheduleDay.MONDAY
            DayOfWeek.TUESDAY -> ScheduleDay.TUESDAY
            DayOfWeek.WEDNESDAY -> ScheduleDay.WEDNESDAY
            DayOfWeek.THURSDAY -> ScheduleDay.THURSDAY
            DayOfWeek.FRIDAY -> ScheduleDay.FRIDAY
            DayOfWeek.SATURDAY -> ScheduleDay.SATURDAY
            DayOfWeek.SUNDAY -> ScheduleDay.SUNDAY
        }

        return availableDays.firstOrNull { it == today } ?: availableDays.first()
    }

    private fun defaultWeekFilter(currentWeekType: PtkCurrentWeekType): ScheduleWeekFilter {
        return when (currentWeekType) {
            PtkCurrentWeekType.UPPER -> ScheduleWeekFilter.UPPER
            PtkCurrentWeekType.LOWER -> ScheduleWeekFilter.LOWER
            PtkCurrentWeekType.UNKNOWN -> ScheduleWeekFilter.ALL
        }
    }
}

class ScheduleViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleViewModel(
                repository = PtkScheduleRepository(),
                preferencesStore = UserPreferencesStore(context.applicationContext)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

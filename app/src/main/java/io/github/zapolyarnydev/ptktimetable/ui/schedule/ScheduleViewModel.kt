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
import java.time.Instant
import java.util.Locale

data class ScheduleLessonItem(
    val dayOfWeek: String,
    val timeRange: String,
    val weekType: PtkWeekType,
    val subject: String,
    val teacher: String?,
    val classroom: String?,
    val rawText: String
)

data class ScheduleUiState(
    val isLoading: Boolean = false,
    val groups: List<PtkGroupInfo> = emptyList(),
    val selectedGroup: PtkGroupInfo? = null,
    val lessons: List<ScheduleLessonItem> = emptyList(),
    val currentWeekType: PtkCurrentWeekType = PtkCurrentWeekType.UNKNOWN,
    val groupsUpdatedAt: Instant? = null,
    val scheduleUpdatedAt: Instant? = null,
    val errorMessage: String? = null
)

class ScheduleViewModel(
    private val repository: ScheduleRepository,
    private val preferencesStore: UserPreferencesStore,
    private val lessonTextNormalizer: LessonTextNormalizer = LessonTextNormalizer(),
    private val nowProvider: () -> Instant = { Instant.now() }
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduleUiState(isLoading = true))
    val state: StateFlow<ScheduleUiState> = _state.asStateFlow()

    init {
        loadGroups(restoreLastSelection = true)
    }

    fun loadGroups() {
        loadGroups(restoreLastSelection = true)
    }

    fun refreshCurrent() {
        val selected = state.value.selectedGroup
        if (selected == null) {
            loadGroups(restoreLastSelection = true)
        } else {
            openGroupInternal(selected, saveAsLastSelected = false)
        }
    }

    fun openGroup(group: PtkGroupInfo) {
        openGroupInternal(group, saveAsLastSelected = true)
    }

    fun backToGroups() {
        _state.update { it.copy(selectedGroup = null, lessons = emptyList(), errorMessage = null) }
    }

    private fun loadGroups(restoreLastSelection: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    selectedGroup = null,
                    lessons = emptyList()
                )
            }

            runCatching {
                val groups = repository.getGroups()
                    .sortedWith(compareBy<PtkGroupInfo> { it.course }.thenBy { it.groupName })
                val currentWeekType = repository.getCurrentWeekType()
                val lastSelected = if (restoreLastSelection) {
                    preferencesStore.getLastSelectedGroupName()
                } else {
                    null
                }
                Triple(groups, currentWeekType, lastSelected)
            }.onSuccess { (groups, weekType, lastSelectedGroupName) ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        groups = groups,
                        currentWeekType = weekType,
                        groupsUpdatedAt = nowProvider()
                    )
                }

                if (!lastSelectedGroupName.isNullOrBlank()) {
                    val restoredGroup = groups.firstOrNull { sameGroup(it.groupName, lastSelectedGroupName) }
                    if (restoredGroup != null) {
                        openGroupInternal(restoredGroup, saveAsLastSelected = false)
                    }
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "Failed to load groups")
                }
            }
        }
    }

    private fun openGroupInternal(
        group: PtkGroupInfo,
        saveAsLastSelected: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isLoading = true, errorMessage = null, selectedGroup = group) }

            runCatching {
                if (saveAsLastSelected) {
                    runCatching { preferencesStore.setLastSelectedGroupName(group.groupName) }
                }

                repository.getScheduleForGroup(group.groupName)
                    .map { raw ->
                        val normalized = lessonTextNormalizer.normalize(raw.rawText)
                        ScheduleLessonItem(
                            dayOfWeek = raw.dayOfWeek,
                            timeRange = raw.timeRange,
                            weekType = raw.weekType,
                            subject = normalized.subject,
                            teacher = normalized.teacher,
                            classroom = normalized.classroom,
                            rawText = raw.rawText
                        )
                    }
            }.onSuccess { lessons ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        selectedGroup = group,
                        lessons = lessons,
                        scheduleUpdatedAt = nowProvider()
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        selectedGroup = group,
                        errorMessage = error.message ?: "Failed to load schedule",
                        lessons = emptyList()
                    )
                }
            }
        }
    }

    private fun sameGroup(left: String, right: String): Boolean {
        return left.trim().lowercase(Locale.ROOT) == right.trim().lowercase(Locale.ROOT)
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

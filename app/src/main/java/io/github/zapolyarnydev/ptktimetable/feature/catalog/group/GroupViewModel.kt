package io.github.zapolyarnydev.ptktimetable.feature.catalog.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Group
import io.github.zapolyarnydev.ptktimetable.domain.schedule.repository.TimetableRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupViewModel(private val timetableRepository: TimetableRepository) : ViewModel() {

    private val _state = MutableStateFlow(GroupUiState())
    val state: StateFlow<GroupUiState> = _state.asStateFlow()

    private val _events = Channel<GroupUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var loadJob: Job? = null

    fun onAction(action: GroupUiAction) {
        when (action) {
            is GroupUiAction.LoadCourse -> {
                if (state.value.courseId != action.courseId) {
                    _state.update { GroupUiState(courseId = action.courseId) }
                }
                load(action.courseId)
            }

            GroupUiAction.Refresh,
            GroupUiAction.Retry,
            -> state.value.courseId?.let(::load)

            is GroupUiAction.SelectGroup -> {
                viewModelScope.launch {
                    _events.send(GroupUiEvent.OpenSchedule(action.groupId))
                }
            }

            GroupUiAction.Back -> {
                viewModelScope.launch {
                    _events.send(GroupUiEvent.NavigateBack)
                }
            }
        }
    }

    private fun load(courseId: Int) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val cached = timetableRepository.observeGroups().first()
                applySnapshot(cached.data, cached.updatedAt, courseId, refreshing = true)
                timetableRepository.refreshGroups()
                val refreshed = timetableRepository.observeGroups().first()
                applySnapshot(refreshed.data, refreshed.updatedAt, courseId, refreshing = false)
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                _state.update {
                    it.copy(
                        isInitialLoading = false,
                        isRefreshing = false,
                        syncError = error.message ?: "Не удалось обновить список групп",
                        isOffline = it.groups.isNotEmpty(),
                    )
                }
            }
        }
    }

    private fun applySnapshot(
        allGroups: List<Group>,
        updatedAt: java.time.Instant?,
        courseId: Int,
        refreshing: Boolean,
    ) {
        val groups = allGroups
            .filter { it.course == courseId }
            .sortedBy { it.groupName }
        val hasCache = groups.isNotEmpty()
        _state.update {
            it.copy(
                courseId = courseId,
                courseTitle = allGroups.firstOrNull { group -> group.course == courseId }?.courseName
                    ?.takeIf(String::isNotBlank)
                    ?: "$courseId курс",
                groups = groups,
                isInitialLoading = refreshing && !hasCache,
                isRefreshing = refreshing && hasCache,
                syncError = null,
                isOffline = false,
                lastUpdatedAt = updatedAt,
            )
        }
    }
}

class GroupViewModelFactory(private val timetableRepository: TimetableRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroupViewModel(timetableRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

package io.github.zapolyarnydev.ptktimetable.feature.catalog.course

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

class CourseViewModel(private val timetableRepository: TimetableRepository) : ViewModel() {

    private val _state = MutableStateFlow(CourseUiState())
    val state: StateFlow<CourseUiState> = _state.asStateFlow()

    private val _events = Channel<CourseUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun onAction(action: CourseUiAction) {
        when (action) {
            CourseUiAction.Refresh,
            CourseUiAction.Retry,
            -> load()

            is CourseUiAction.SelectCourse -> {
                viewModelScope.launch {
                    _events.send(CourseUiEvent.OpenGroups(action.courseId))
                }
            }
        }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val cached = timetableRepository.observeGroups().first()
                applySnapshot(cached.data, cached.updatedAt, refreshing = true)
                timetableRepository.refreshGroups()
                val refreshed = timetableRepository.observeGroups().first()
                applySnapshot(refreshed.data, refreshed.updatedAt, refreshing = false)
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                _state.update {
                    it.copy(
                        isInitialLoading = false,
                        isRefreshing = false,
                        syncError = error.message ?: "Не удалось обновить список курсов",
                        isOffline = it.courses.isNotEmpty(),
                    )
                }
            }
        }
    }

    private fun applySnapshot(groups: List<Group>, updatedAt: java.time.Instant?, refreshing: Boolean) {
        val courses = groups
            .groupBy { it.course }
            .map { (course, items) ->
                CourseUiItem(
                    id = course,
                    title = items.firstOrNull()?.courseName?.takeIf(String::isNotBlank) ?: "$course курс",
                )
            }
            .sortedBy { it.id }
        val hasCache = courses.isNotEmpty()
        _state.update {
            it.copy(
                courses = courses,
                isInitialLoading = refreshing && !hasCache,
                isRefreshing = refreshing && hasCache,
                syncError = null,
                isOffline = false,
                lastUpdatedAt = updatedAt,
            )
        }
    }
}

class CourseViewModelFactory(private val timetableRepository: TimetableRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CourseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CourseViewModel(timetableRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

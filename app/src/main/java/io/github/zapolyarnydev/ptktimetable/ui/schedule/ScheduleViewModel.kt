package io.github.zapolyarnydev.ptktimetable.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.zapolyarnydev.ptktimetable.data.model.PtkCurrentWeekType
import io.github.zapolyarnydev.ptktimetable.data.model.PtkGroupInfo
import io.github.zapolyarnydev.ptktimetable.data.model.PtkRawLesson
import io.github.zapolyarnydev.ptktimetable.data.repository.PtkScheduleRepository
import io.github.zapolyarnydev.ptktimetable.data.repository.ScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScheduleUiState(
    val isLoading: Boolean = false,
    val groups: List<PtkGroupInfo> = emptyList(),
    val selectedGroup: PtkGroupInfo? = null,
    val lessons: List<PtkRawLesson> = emptyList(),
    val currentWeekType: PtkCurrentWeekType = PtkCurrentWeekType.UNKNOWN,
    val errorMessage: String? = null
)

class ScheduleViewModel(
    private val repository: ScheduleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduleUiState(isLoading = true))
    val state: StateFlow<ScheduleUiState> = _state.asStateFlow()

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isLoading = true, errorMessage = null, selectedGroup = null, lessons = emptyList()) }
            runCatching {
                val groups = repository.getGroups()
                val currentWeek = repository.getCurrentWeekType()
                groups to currentWeek
            }.onSuccess { (groups, weekType) ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        groups = groups.sortedWith(compareBy<PtkGroupInfo> { it.course }.thenBy { it.groupName }),
                        currentWeekType = weekType
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "Failed to load groups")
                }
            }
        }
    }

    fun openGroup(group: PtkGroupInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isLoading = true, errorMessage = null, selectedGroup = group) }
            runCatching { repository.getScheduleForGroup(group.groupName) }
                .onSuccess { lessons ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            selectedGroup = group,
                            lessons = lessons
                        )
                    }
                }
                .onFailure { error ->
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

    fun backToGroups() {
        _state.update { it.copy(selectedGroup = null, lessons = emptyList(), errorMessage = null) }
    }
}

class ScheduleViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleViewModel(PtkScheduleRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

package io.github.zapolyarnydev.ptktimetable.feature.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.zapolyarnydev.ptktimetable.data.local.UserPreferencesStore
import io.github.zapolyarnydev.ptktimetable.domain.schedule.repository.TimetableRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppNavigationViewModel(
    private val timetableRepository: TimetableRepository,
    private val preferencesStore: UserPreferencesStore,
) : ViewModel() {

    private val _state = MutableStateFlow(AppNavigationUiState())
    val state: StateFlow<AppNavigationUiState> = _state.asStateFlow()

    init {
        restoreLastSchedule()
    }

    private fun restoreLastSchedule() {
        viewModelScope.launch(Dispatchers.IO) {
            val restoredSchedule = runCatching {
                val groupId = preferencesStore.getLastSelectedGroupName() ?: return@runCatching null
                val group = timetableRepository.observeGroups().first().data
                    .firstOrNull { it.groupName.equals(groupId, ignoreCase = true) }
                    ?: return@runCatching null
                RestoredScheduleRoute(courseId = group.course, groupId = group.groupName)
            }.getOrNull()

            _state.update {
                it.copy(
                    isRestoring = false,
                    restoredSchedule = restoredSchedule,
                )
            }
        }
    }
}

class AppNavigationViewModelFactory(
    private val timetableRepository: TimetableRepository,
    private val preferencesStore: UserPreferencesStore,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppNavigationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppNavigationViewModel(timetableRepository, preferencesStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

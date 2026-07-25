package io.github.zapolyarnydev.ptktimetable.feature.catalog.group

import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Group
import java.time.Instant

data class GroupUiState(
    val courseId: Int? = null,
    val courseTitle: String = "",
    val groups: List<Group> = emptyList(),
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val syncError: String? = null,
    val isOffline: Boolean = false,
    val lastUpdatedAt: Instant? = null,
)

sealed interface GroupUiAction {
    data class LoadCourse(val courseId: Int) : GroupUiAction

    data object Refresh : GroupUiAction

    data object Retry : GroupUiAction

    data class SelectGroup(val groupId: String) : GroupUiAction

    data object Back : GroupUiAction
}

sealed interface GroupUiEvent {
    data class OpenSchedule(val groupId: String) : GroupUiEvent

    data object NavigateBack : GroupUiEvent
}

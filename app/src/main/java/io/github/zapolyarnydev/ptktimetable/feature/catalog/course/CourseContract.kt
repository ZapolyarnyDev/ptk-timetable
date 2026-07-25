package io.github.zapolyarnydev.ptktimetable.feature.catalog.course

import java.time.Instant

data class CourseUiItem(val id: Int, val title: String)

data class CourseUiState(
    val courses: List<CourseUiItem> = emptyList(),
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val syncError: String? = null,
    val isOffline: Boolean = false,
    val lastUpdatedAt: Instant? = null,
)

sealed interface CourseUiAction {
    data object Refresh : CourseUiAction

    data object Retry : CourseUiAction

    data class SelectCourse(val courseId: Int) : CourseUiAction
}

sealed interface CourseUiEvent {
    data class OpenGroups(val courseId: Int) : CourseUiEvent
}

package io.github.zapolyarnydev.ptktimetable.feature.navigation

data class RestoredScheduleRoute(val courseId: Int, val groupId: String)

data class AppNavigationUiState(val isRestoring: Boolean = true, val restoredSchedule: RestoredScheduleRoute? = null)

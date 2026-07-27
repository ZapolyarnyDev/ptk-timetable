package io.github.zapolyarnydev.ptktimetable.feature.catalog.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zapolyarnydev.ptktimetable.core.ui.EmptyStateBlock
import io.github.zapolyarnydev.ptktimetable.core.ui.FullScreenErrorState
import io.github.zapolyarnydev.ptktimetable.core.ui.FullScreenLoadingState
import io.github.zapolyarnydev.ptktimetable.core.ui.HeaderPanel
import io.github.zapolyarnydev.ptktimetable.core.ui.SelectionListSection
import io.github.zapolyarnydev.ptktimetable.core.ui.SelectionListSkeleton
import io.github.zapolyarnydev.ptktimetable.feature.catalog.CatalogStatusCard
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppDimensions
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppIcons
import io.github.zapolyarnydev.ptktimetable.ui.theme.MaterialThemeAppColors

@Composable
fun GroupRoute(courseId: Int, viewModel: GroupViewModel, onGroupSelected: (String) -> Unit, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(courseId) {
        viewModel.onAction(GroupUiAction.LoadCourse(courseId))
    }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is GroupUiEvent.OpenSchedule -> onGroupSelected(event.groupId)
                GroupUiEvent.NavigateBack -> onBack()
            }
        }
    }
    GroupScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun GroupScreen(state: GroupUiState, onAction: (GroupUiAction) -> Unit) {
    Scaffold(containerColor = MaterialThemeAppColors.background) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isInitialLoading && state.groups.isEmpty() -> FullScreenLoadingState(
                    title = "Загружаем группы",
                    message = "Покажем кеш, если он уже есть",
                )

                state.syncError != null && state.groups.isEmpty() -> FullScreenErrorState(
                    title = "Группы недоступны",
                    message = state.syncError,
                    onRetry = { onAction(GroupUiAction.Retry) },
                    secondaryAction = "К курсам" to { onAction(GroupUiAction.Back) },
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        AppDimensions.screenHorizontalPadding,
                        AppDimensions.screenVerticalPadding,
                    ),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.sectionSpacing),
                ) {
                    item {
                        HeaderPanel(
                            title = "Выберите группу",
                            subtitle = state.courseTitle.ifBlank { "Курс не выбран" },
                            icon = AppIcons.group,
                        )
                    }
                    item {
                        CatalogStatusCard(
                            title = "Группа для расписания",
                            subtitle = "Шаг 2 из 2",
                            lastUpdatedAt = state.lastUpdatedAt,
                            isRefreshing = state.isRefreshing,
                            syncError = state.syncError,
                            isOffline = state.isOffline,
                            onRefresh = { onAction(GroupUiAction.Refresh) },
                            secondaryAction = "К курсам" to { onAction(GroupUiAction.Back) },
                        )
                    }
                    item {
                        when {
                            state.isInitialLoading -> SelectionListSkeleton(rows = 6)

                            state.groups.isEmpty() -> EmptyStateBlock("Для выбранного курса группы не найдены")

                            else -> SelectionListSection(
                                title = "Доступные группы",
                                items = state.groups,
                                icon = { AppIcons.group },
                                titleText = { "Группа ${it.groupName}" },
                                subtitleText = { it.collegeName },
                                onClick = { onAction(GroupUiAction.SelectGroup(it.groupName)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

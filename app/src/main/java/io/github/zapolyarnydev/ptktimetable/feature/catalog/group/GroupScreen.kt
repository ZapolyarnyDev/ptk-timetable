package io.github.zapolyarnydev.ptktimetable.feature.catalog.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zapolyarnydev.ptktimetable.core.ui.EmptyStateBlock
import io.github.zapolyarnydev.ptktimetable.core.ui.FullScreenErrorState
import io.github.zapolyarnydev.ptktimetable.core.ui.FullScreenLoadingState
import io.github.zapolyarnydev.ptktimetable.core.ui.HeaderBackButton
import io.github.zapolyarnydev.ptktimetable.core.ui.HeaderTitleRow
import io.github.zapolyarnydev.ptktimetable.core.ui.SelectionListSection
import io.github.zapolyarnydev.ptktimetable.core.ui.SelectionListSkeleton
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppDimensions
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppIcons
import io.github.zapolyarnydev.ptktimetable.ui.theme.MaterialThemeAppColors

@Composable
fun GroupRoute(
    courseId: Int,
    viewModel: GroupViewModel,
    onGroupSelected: (String) -> Unit,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
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
    GroupScreen(state = state, onAction = viewModel::onAction, onOpenSettings = onOpenSettings)
}

@Composable
fun GroupScreen(state: GroupUiState, onAction: (GroupUiAction) -> Unit, onOpenSettings: () -> Unit = {}) {
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
                    contentPadding = PaddingValues(vertical = AppDimensions.screenVerticalPadding),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.sectionSpacing),
                ) {
                    item {
                        Box(Modifier.padding(horizontal = AppDimensions.screenHorizontalPadding)) {
                            Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.compactSpacing)) {
                                HeaderTitleRow(
                                    title = "Выберите группу",
                                    icon = AppIcons.group,
                                    onOpenSettings = onOpenSettings,
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    HeaderBackButton("К курсам") { onAction(GroupUiAction.Back) }
                                    Text(
                                        state.courseTitle.ifBlank { "Курс не выбран" },
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialThemeAppColors.textSecondary,
                                    )
                                    IconButton(
                                        onClick = { onAction(GroupUiAction.Refresh) },
                                        enabled = !state.isRefreshing,
                                    ) {
                                        Icon(AppIcons.refresh, contentDescription = "Обновить")
                                    }
                                }
                            }
                        }
                    }
                    item {
                        when {
                            state.isInitialLoading -> SelectionListSkeleton(rows = 6)

                            state.groups.isEmpty() -> Box(
                                Modifier.padding(horizontal = AppDimensions.screenHorizontalPadding),
                            ) {
                                EmptyStateBlock("Для выбранного курса группы не найдены")
                            }

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

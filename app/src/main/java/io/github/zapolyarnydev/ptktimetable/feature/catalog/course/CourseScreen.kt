package io.github.zapolyarnydev.ptktimetable.feature.catalog.course

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
fun CourseRoute(viewModel: CourseViewModel, onCourseSelected: (Int) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CourseUiEvent.OpenGroups -> onCourseSelected(event.courseId)
            }
        }
    }
    CourseScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun CourseScreen(state: CourseUiState, onAction: (CourseUiAction) -> Unit) {
    Scaffold(containerColor = MaterialThemeAppColors.background) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isInitialLoading && state.courses.isEmpty() -> FullScreenLoadingState(
                    title = "Загружаем курсы",
                    message = "Сначала проверяем сохранённые данные",
                )

                state.syncError != null && state.courses.isEmpty() -> FullScreenErrorState(
                    title = "Курсы недоступны",
                    message = state.syncError,
                    onRetry = { onAction(CourseUiAction.Retry) },
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
                            title = "Твоё расписание",
                            subtitle = "Выбери курс — дальше покажем доступные группы",
                            icon = AppIcons.schedule,
                        )
                    }
                    item {
                        CatalogStatusCard(
                            title = "Выберите курс",
                            subtitle = "Шаг 1 из 2",
                            lastUpdatedAt = state.lastUpdatedAt,
                            isRefreshing = state.isRefreshing,
                            syncError = state.syncError,
                            isOffline = state.isOffline,
                            onRefresh = { onAction(CourseUiAction.Refresh) },
                        )
                    }
                    item {
                        when {
                            state.isInitialLoading -> SelectionListSkeleton(rows = 4)

                            state.courses.isEmpty() -> EmptyStateBlock("Курсы не найдены")

                            else -> SelectionListSection(
                                title = "Доступные курсы",
                                items = state.courses,
                                icon = { AppIcons.courseList },
                                titleText = { it.title },
                                subtitleText = { "Курс №${it.id}" },
                                onClick = { onAction(CourseUiAction.SelectCourse(it.id)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

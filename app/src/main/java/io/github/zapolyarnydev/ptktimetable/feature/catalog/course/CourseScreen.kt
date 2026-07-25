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
import io.github.zapolyarnydev.ptktimetable.feature.catalog.CatalogStatusCard
import io.github.zapolyarnydev.ptktimetable.ui.schedule.EmptyStateBlock
import io.github.zapolyarnydev.ptktimetable.ui.schedule.HeaderPanel
import io.github.zapolyarnydev.ptktimetable.ui.schedule.SelectionListSection
import io.github.zapolyarnydev.ptktimetable.ui.schedule.SelectionListSkeleton
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
    Scaffold(containerColor = MaterialThemeAppColors.canvas) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
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

package io.github.zapolyarnydev.ptktimetable.feature.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.zapolyarnydev.ptktimetable.AppContainer
import io.github.zapolyarnydev.ptktimetable.feature.catalog.course.CourseRoute
import io.github.zapolyarnydev.ptktimetable.feature.catalog.course.CourseViewModel
import io.github.zapolyarnydev.ptktimetable.feature.catalog.group.GroupRoute
import io.github.zapolyarnydev.ptktimetable.feature.catalog.group.GroupViewModel
import io.github.zapolyarnydev.ptktimetable.feature.notes.NotesViewModel
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleRoute
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleViewModel

@Composable
fun AppNavigation(container: AppContainer, navigationViewModel: AppNavigationViewModel) {
    val restoration by navigationViewModel.state.collectAsStateWithLifecycle()
    if (restoration.isRestoring) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val navController = rememberNavController()
    var restorationApplied by rememberSaveable { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = AppRoute.COURSES,
    ) {
        composable(AppRoute.COURSES) {
            val courseViewModel: CourseViewModel = viewModel(factory = container.courseViewModelFactory)
            CourseRoute(
                viewModel = courseViewModel,
                onCourseSelected = { navController.navigate(AppRoute.groups(it)) },
            )
        }
        composable(
            route = AppRoute.GROUPS,
            arguments = listOf(navArgument(AppRoute.COURSE_ID) { type = NavType.IntType }),
        ) { entry ->
            val courseId = requireNotNull(entry.arguments?.getInt(AppRoute.COURSE_ID))
            val groupViewModel: GroupViewModel = viewModel(factory = container.groupViewModelFactory)
            GroupRoute(
                courseId = courseId,
                viewModel = groupViewModel,
                onGroupSelected = { navController.navigate(AppRoute.schedule(it)) },
                onBack = { navController.navigateUp() },
            )
        }
        composable(
            route = AppRoute.SCHEDULE,
            arguments = listOf(navArgument(AppRoute.GROUP_ID) { type = NavType.StringType }),
        ) { entry ->
            val groupId = Uri.decode(requireNotNull(entry.arguments?.getString(AppRoute.GROUP_ID)))
            val scheduleViewModel: ScheduleViewModel = viewModel(factory = container.scheduleViewModelFactory)
            val notesViewModel: NotesViewModel = viewModel(factory = container.notesViewModelFactory)
            LaunchedEffect(groupId) {
                scheduleViewModel.openGroup(groupId)
            }
            ScheduleRoute(
                viewModel = scheduleViewModel,
                notesViewModel = notesViewModel,
                onBack = { navController.navigateUp() },
            )
        }
    }

    LaunchedEffect(restoration.restoredSchedule, restorationApplied) {
        if (restorationApplied) return@LaunchedEffect
        restorationApplied = true
        restoration.restoredSchedule?.let { restored ->
            navController.navigate(AppRoute.groups(restored.courseId))
            navController.navigate(AppRoute.schedule(restored.groupId))
        }
    }
}

private object AppRoute {
    const val COURSE_ID = "courseId"
    const val GROUP_ID = "groupId"
    const val COURSES = "courses"
    const val GROUPS = "groups/{$COURSE_ID}"
    const val SCHEDULE = "schedule/{$GROUP_ID}"

    fun groups(courseId: Int): String = "groups/$courseId"

    fun schedule(groupId: String): String = "schedule/${Uri.encode(groupId)}"
}

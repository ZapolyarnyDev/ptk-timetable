package io.github.zapolyarnydev.ptktimetable

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.zapolyarnydev.ptktimetable.feature.catalog.course.CourseRoute
import io.github.zapolyarnydev.ptktimetable.feature.catalog.course.CourseViewModel
import io.github.zapolyarnydev.ptktimetable.feature.catalog.group.GroupRoute
import io.github.zapolyarnydev.ptktimetable.feature.catalog.group.GroupViewModel
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleScreen
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleViewModel
import io.github.zapolyarnydev.ptktimetable.ui.theme.PtkTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationsPermissionIfNeeded()
        setContent {
            PtkTheme {
                val container = (application as PtkApplication).container
                val courseViewModel: CourseViewModel = viewModel(factory = container.courseViewModelFactory)
                val groupViewModel: GroupViewModel = viewModel(factory = container.groupViewModelFactory)
                val scheduleViewModel: ScheduleViewModel = viewModel(factory = container.scheduleViewModelFactory)
                var route by rememberSaveable { mutableStateOf(CatalogRoute.COURSES) }
                var courseId by rememberSaveable { mutableIntStateOf(0) }
                var groupId by rememberSaveable { mutableStateOf("") }

                when (route) {
                    CatalogRoute.COURSES -> CourseRoute(
                        viewModel = courseViewModel,
                        onCourseSelected = {
                            courseId = it
                            route = CatalogRoute.GROUPS
                        },
                    )

                    CatalogRoute.GROUPS -> {
                        BackHandler { route = CatalogRoute.COURSES }
                        GroupRoute(
                            courseId = courseId,
                            viewModel = groupViewModel,
                            onGroupSelected = {
                                groupId = it
                                route = CatalogRoute.SCHEDULE
                            },
                            onBack = { route = CatalogRoute.COURSES },
                        )
                    }

                    CatalogRoute.SCHEDULE -> {
                        BackHandler { route = CatalogRoute.GROUPS }
                        LaunchedEffect(groupId) {
                            scheduleViewModel.openGroup(groupId)
                        }
                        ScheduleScreen(
                            state = scheduleViewModel.state,
                            onRefresh = scheduleViewModel::refreshCurrent,
                            onBackToGroups = { route = CatalogRoute.GROUPS },
                            onSelectMode = scheduleViewModel::selectMode,
                            onSelectDay = scheduleViewModel::selectDay,
                            onPreviousDay = scheduleViewModel::previousDay,
                            onNextDay = scheduleViewModel::nextDay,
                            onSelectDate = scheduleViewModel::selectDate,
                            onPreviousDate = scheduleViewModel::previousDate,
                            onNextDate = scheduleViewModel::nextDate,
                            onGoToToday = scheduleViewModel::goToToday,
                            onSelectWeekFilter = scheduleViewModel::selectWeekFilter,
                            onSaveLessonNote = scheduleViewModel::saveNoteForLesson,
                            onSetLessonReminder = scheduleViewModel::setReminderForLesson,
                            onDeleteLessonNote = scheduleViewModel::deleteNoteForLesson,
                            onUpdateNoteById = scheduleViewModel::updateNoteById,
                            onDeleteNoteById = scheduleViewModel::deleteNoteById,
                        )
                    }
                }
            }
        }
    }

    private fun requestNotificationsPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) return
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            1001,
        )
    }
}

private enum class CatalogRoute {
    COURSES,
    GROUPS,
    SCHEDULE,
}

package io.github.zapolyarnydev.ptktimetable

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleScreen
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleViewModel
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleViewModelFactory
import io.github.zapolyarnydev.ptktimetable.ui.theme.PtkTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationsPermissionIfNeeded()
        setContent {
            PtkTheme {
                val vm: ScheduleViewModel = viewModel(
                    factory = ScheduleViewModelFactory(applicationContext)
                )
                ScheduleScreen(
                    state = vm.state,
                    onRetry = vm::refreshCurrent,
                    onRefresh = vm::refreshCurrent,
                    onCourseSelect = vm::selectCourse,
                    onGroupSelect = vm::openGroup,
                    onBackToCourses = vm::backToCourses,
                    onBackToGroups = vm::backToGroups,
                    onSelectMode = vm::selectMode,
                    onSelectDay = vm::selectDay,
                    onPreviousDay = vm::previousDay,
                    onNextDay = vm::nextDay,
                    onSelectDate = vm::selectDate,
                    onPreviousDate = vm::previousDate,
                    onNextDate = vm::nextDate,
                    onGoToToday = vm::goToToday,
                    onSelectWeekFilter = vm::selectWeekFilter,
                    onSaveLessonNote = vm::saveNoteForLesson,
                    onSetLessonReminder = vm::setReminderForLesson,
                    onDeleteLessonNote = vm::deleteNoteForLesson,
                    onUpdateNoteById = vm::updateNoteById,
                    onDeleteNoteById = vm::deleteNoteById
                )
            }
        }
    }

    private fun requestNotificationsPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) return
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            1001
        )
    }
}

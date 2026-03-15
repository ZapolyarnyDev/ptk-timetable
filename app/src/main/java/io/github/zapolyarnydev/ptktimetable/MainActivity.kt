package io.github.zapolyarnydev.ptktimetable

import android.os.Bundle
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
                    onSelectWeekFilter = vm::selectWeekFilter
                )
            }
        }
    }
}

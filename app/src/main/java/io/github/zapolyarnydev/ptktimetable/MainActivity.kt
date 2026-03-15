package io.github.zapolyarnydev.ptktimetable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleScreen
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleViewModel
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: ScheduleViewModel = viewModel(
                factory = ScheduleViewModelFactory(applicationContext)
            )
            ScheduleScreen(
                state = vm.state,
                onRetry = vm::refreshCurrent,
                onRefresh = vm::refreshCurrent,
                onGroupClick = vm::openGroup,
                onBack = vm::backToGroups
            )
        }
    }
}

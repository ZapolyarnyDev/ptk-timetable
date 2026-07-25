package io.github.zapolyarnydev.ptktimetable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.zapolyarnydev.ptktimetable.feature.navigation.AppNavigation
import io.github.zapolyarnydev.ptktimetable.feature.navigation.AppNavigationViewModel
import io.github.zapolyarnydev.ptktimetable.ui.theme.PtkTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PtkTheme {
                val container = (application as PtkApplication).container
                val navigationViewModel: AppNavigationViewModel =
                    viewModel(factory = container.appNavigationViewModelFactory)
                AppNavigation(
                    container = container,
                    navigationViewModel = navigationViewModel,
                )
            }
        }
    }
}

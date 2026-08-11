package io.github.zapolyarnydev.ptktimetable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.zapolyarnydev.ptktimetable.data.preferences.AppearancePreferences
import io.github.zapolyarnydev.ptktimetable.feature.navigation.AppNavigation
import io.github.zapolyarnydev.ptktimetable.feature.navigation.AppNavigationViewModel
import io.github.zapolyarnydev.ptktimetable.ui.theme.PtkTheme
import io.github.zapolyarnydev.ptktimetable.ui.theme.toThemeSettings

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val container = (application as PtkApplication).container
            val appearance by container.appearancePreferencesRepository.preferences.collectAsStateWithLifecycle(
                initialValue = AppearancePreferences.Defaults,
            )
            PtkTheme(settings = appearance.toThemeSettings()) {
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

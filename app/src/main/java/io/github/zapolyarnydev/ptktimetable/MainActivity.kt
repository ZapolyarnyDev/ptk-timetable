package io.github.zapolyarnydev.ptktimetable

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.zapolyarnydev.ptktimetable.feature.navigation.AppNavigation
import io.github.zapolyarnydev.ptktimetable.feature.navigation.AppNavigationViewModel
import io.github.zapolyarnydev.ptktimetable.ui.theme.PtkTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationsPermissionIfNeeded()
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

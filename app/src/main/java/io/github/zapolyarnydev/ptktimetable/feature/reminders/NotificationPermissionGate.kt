package io.github.zapolyarnydev.ptktimetable.feature.reminders

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import io.github.zapolyarnydev.ptktimetable.feature.notes.NotesUiAction

@Composable
fun rememberPermissionAwareNotesAction(onAction: (NotesUiAction) -> Unit): (NotesUiAction) -> Unit {
    val context = LocalContext.current
    val currentOnAction by rememberUpdatedState(onAction)
    var pendingAction by remember { mutableStateOf<NotesUiAction.SaveReminder?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val action = pendingAction
        pendingAction = null
        if (granted && action != null) {
            currentOnAction(action)
        } else if (!granted) {
            currentOnAction(NotesUiAction.NotificationPermissionDenied)
        }
    }

    return remember(context, permissionLauncher) {
        { action ->
            val needsPermission = action is NotesUiAction.SaveReminder &&
                action.enabled &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            if (needsPermission) {
                pendingAction = action as NotesUiAction.SaveReminder
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                currentOnAction(action)
            }
        }
    }
}

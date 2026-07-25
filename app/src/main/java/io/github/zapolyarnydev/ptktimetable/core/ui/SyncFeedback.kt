package io.github.zapolyarnydev.ptktimetable.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppIcons
import io.github.zapolyarnydev.ptktimetable.ui.theme.MaterialThemeAppColors
import java.time.Instant

@Composable
internal fun SyncFeedback(updatedAt: Instant?, isRefreshing: Boolean, syncError: String?, isOffline: Boolean) {
    val colors = MaterialThemeAppColors
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            isRefreshing -> {
                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                Text("Обновляем данные…", style = MaterialTheme.typography.bodySmall)
            }

            syncError != null -> {
                Icon(
                    AppIcons.warning,
                    contentDescription = null,
                    tint = colors.warning,
                    modifier = Modifier.size(17.dp),
                )
                Text(
                    if (isOffline) "Офлайн · показаны сохранённые данные" else "Не удалось обновить данные",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.warning,
                )
            }

            updatedAt != null -> {
                Icon(
                    AppIcons.time,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    "Обновлено ${formatInstant(updatedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> Spacer(Modifier)
        }
    }
}

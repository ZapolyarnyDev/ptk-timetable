package io.github.zapolyarnydev.ptktimetable.feature.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zapolyarnydev.ptktimetable.core.ui.InfoPanel
import io.github.zapolyarnydev.ptktimetable.core.ui.OutlinedActionButton
import io.github.zapolyarnydev.ptktimetable.core.ui.PrimaryActionButton
import io.github.zapolyarnydev.ptktimetable.core.ui.formatInstant
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppIcons
import java.time.Instant

@Composable
internal fun CatalogStatusCard(
    title: String,
    subtitle: String,
    lastUpdatedAt: Instant?,
    isRefreshing: Boolean,
    syncError: String?,
    isOffline: Boolean,
    onRefresh: () -> Unit,
    secondaryAction: Pair<String, () -> Unit>? = null,
) {
    InfoPanel {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (lastUpdatedAt != null) {
                Text(
                    "Обновлено ${formatInstant(lastUpdatedAt)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isRefreshing) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(Modifier.height(16.dp), strokeWidth = 2.dp)
                    Text("Обновляем данные…", style = MaterialTheme.typography.bodySmall)
                }
            }
            if (syncError != null) {
                Text(
                    if (isOffline) {
                        "Офлайн. Показаны сохранённые данные. $syncError"
                    } else {
                        syncError
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (secondaryAction != null) {
                OutlinedActionButton(
                    text = secondaryAction.first,
                    onClick = secondaryAction.second,
                    modifier = Modifier.weight(1f),
                )
            }
            PrimaryActionButton(
                text = "Обновить",
                onClick = onRefresh,
                icon = AppIcons.refresh,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

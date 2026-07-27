package io.github.zapolyarnydev.ptktimetable.feature.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zapolyarnydev.ptktimetable.core.ui.SyncFeedback
import io.github.zapolyarnydev.ptktimetable.core.ui.TransparentSection
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppDimensions
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppIcons
import io.github.zapolyarnydev.ptktimetable.ui.theme.MaterialThemeAppColors
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
    val colors = MaterialThemeAppColors
    TransparentSection(padding = 0.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.compactSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
            if (secondaryAction != null) {
                TextButton(onClick = secondaryAction.second) { Text(secondaryAction.first) }
            }
            IconButton(
                onClick = onRefresh,
                enabled = !isRefreshing,
            ) {
                Icon(AppIcons.refresh, contentDescription = "Обновить")
            }
        }
        if (lastUpdatedAt != null || isRefreshing || syncError != null || isOffline) {
            SyncFeedback(
                updatedAt = lastUpdatedAt,
                isRefreshing = isRefreshing,
                syncError = syncError,
                isOffline = isOffline,
                modifier = Modifier.padding(top = AppDimensions.compactSpacing),
            )
        }
    }
}

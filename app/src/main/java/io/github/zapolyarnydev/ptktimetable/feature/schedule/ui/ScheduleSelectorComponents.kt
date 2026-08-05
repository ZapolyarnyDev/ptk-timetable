package io.github.zapolyarnydev.ptktimetable.feature.schedule.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.zapolyarnydev.ptktimetable.core.ui.AppChoiceChip
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekFilter
import io.github.zapolyarnydev.ptktimetable.ui.schedule.title
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppIcons
import io.github.zapolyarnydev.ptktimetable.ui.theme.MaterialThemeAppColors

@Composable
internal fun WeekSelector(selected: WeekFilter, onSelect: (WeekFilter) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(WeekFilter.entries, key = { it.name }) { filter ->
            AppChoiceChip(
                selected = filter == selected,
                label = filter.title,
                icon = AppIcons.filter,
                onClick = { onSelect(filter) },
                containerColor = Color.Transparent,
                selectedContainerColor = MaterialThemeAppColors.accentMuted,
                labelColor = MaterialThemeAppColors.textSecondary,
                selectedLabelColor = MaterialThemeAppColors.accent,
                iconColor = MaterialThemeAppColors.textSecondary,
                selectedLeadingIconColor = MaterialThemeAppColors.accent,
            )
        }
    }
}

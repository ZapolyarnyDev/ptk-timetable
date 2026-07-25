package io.github.zapolyarnydev.ptktimetable.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppIcons
import io.github.zapolyarnydev.ptktimetable.ui.theme.PtkTheme

@Preview(showBackground = true)
@Composable
private fun CardsAndButtonsPreview() {
    PtkTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HeaderPanel(
                title = "Расписание",
                subtitle = "Компоненты дизайн-системы",
                icon = AppIcons.schedule,
            )
            SectionCard {
                PrimaryActionButton(
                    text = "Обновить",
                    onClick = {},
                    icon = AppIcons.refresh,
                )
                OutlinedActionButton(text = "Повторить", onClick = {})
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingAndEmptyPreview() {
    PtkTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InlineLoading()
            SelectionListSkeleton(rows = 2)
            EmptyStateBlock("Данных пока нет")
        }
    }
}

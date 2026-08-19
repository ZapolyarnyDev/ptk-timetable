package io.github.zapolyarnydev.ptktimetable.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.zapolyarnydev.ptktimetable.core.ui.AppChoiceChip
import io.github.zapolyarnydev.ptktimetable.core.ui.TonalSection
import io.github.zapolyarnydev.ptktimetable.data.preferences.AppearancePreferences
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppIcons
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppShapes
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppThemeMode
import io.github.zapolyarnydev.ptktimetable.ui.theme.PtkTheme
import io.github.zapolyarnydev.ptktimetable.ui.theme.ThemeManager
import io.github.zapolyarnydev.ptktimetable.ui.theme.toThemeSettings

@Composable
internal fun AppearanceTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("appearance-back")) {
            Icon(AppIcons.back, contentDescription = "Назад")
        }
        Column {
            Text("Внешний вид", style = MaterialTheme.typography.headlineSmall)
            Text("Цвета и тема приложения", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
internal fun ThemeModeSelector(selected: AppThemeMode, onSelect: (AppThemeMode) -> Unit) {
    AppearanceSection(title = "Режим темы", subtitle = "Системный режим следует настройке устройства") {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AppChoiceChip(
                selected = selected == AppThemeMode.SYSTEM,
                label = "Системная",
                icon = AppIcons.systemTheme,
                onClick = { onSelect(AppThemeMode.SYSTEM) },
            )
            AppChoiceChip(
                selected = selected == AppThemeMode.LIGHT,
                label = "Светлая",
                icon = AppIcons.lightTheme,
                onClick = { onSelect(AppThemeMode.LIGHT) },
            )
            AppChoiceChip(
                selected = selected == AppThemeMode.DARK,
                label = "Тёмная",
                icon = AppIcons.darkTheme,
                onClick = { onSelect(AppThemeMode.DARK) },
            )
        }
    }
}

@Composable
internal fun AppearancePreview(preferences: AppearancePreferences) {
    PtkTheme(settings = preferences.toThemeSettings()) {
        val palette = ThemeManager.palette(
            preferences.toThemeSettings(),
            ThemeManager.isDark(preferences.toThemeSettings(), isSystemInDarkTheme()),
        )
        Surface(
            modifier = Modifier.fillMaxWidth().testTag("appearance-preview"),
            color = palette.background,
            shape = AppShapes.medium,
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Предпросмотр", color = palette.textPrimary, style = MaterialTheme.typography.titleMedium)
                Surface(color = palette.surfaceMuted, shape = AppShapes.small) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("Мобильная разработка", color = palette.textPrimary, fontWeight = FontWeight.Bold)
                        Text("10.40–12.10 · аудитория 312", color = palette.textSecondary)
                        Text("Акцентный элемент", color = palette.accent, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
internal fun AppearanceColorEditor(
    title: String,
    value: AppearanceColorInput,
    resolvedColor: Color,
    onHexChange: (String) -> Unit,
    onPreset: (Long) -> Unit,
    onReset: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(24.dp).clip(CircleShape).background(resolvedColor))
            Text(title, modifier = Modifier.padding(start = 10.dp).weight(1f), fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onReset) { Text("По умолчанию") }
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            COLOR_PRESETS.forEach { argb ->
                Surface(
                    onClick = { onPreset(argb) },
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = Color(argb.toInt()),
                ) {}
            }
        }
        OutlinedTextField(
            value = value.text,
            onValueChange = onHexChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("HEX (#RRGGBB или #AARRGGBB)") },
            placeholder = { Text("#FF315FEA") },
            singleLine = true,
            isError = !value.isValid,
            supportingText = if (value.isValid) {
                null
            } else {
                { Text("Введите 6 или 8 шестнадцатеричных цифр") }
            },
        )
    }
}

@Composable
internal fun AppearanceSection(title: String, subtitle: String? = null, content: @Composable () -> Unit) {
    TonalSection {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        subtitle?.let {
            Text(it, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

internal val COLOR_PRESETS = listOf(
    0xFF171A22,
    0xFFFFFFFF,
    0xFF5E6576,
    0xFF315FEA,
    0xFF6750A4,
    0xFF008577,
    0xFFD32F2F,
    0xFFFF9800,
)

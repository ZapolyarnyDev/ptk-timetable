package io.github.zapolyarnydev.ptktimetable

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import io.github.zapolyarnydev.ptktimetable.feature.settings.AppearanceScreen
import io.github.zapolyarnydev.ptktimetable.feature.settings.AppearanceUiAction
import io.github.zapolyarnydev.ptktimetable.feature.settings.AppearanceUiState
import io.github.zapolyarnydev.ptktimetable.ui.theme.AppThemeMode
import io.github.zapolyarnydev.ptktimetable.ui.theme.PtkTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicReference

class AppearanceScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun exposesOnlySupportedAppearanceControls() {
        val lastAction = AtomicReference<AppearanceUiAction>()
        composeRule.setContent {
            PtkTheme {
                AppearanceScreen(
                    state = AppearanceUiState(isLoading = false),
                    onAction = (lastAction::set),
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText("Системная").assertExists().performClick()
        assertEquals(AppearanceUiAction.SelectThemeMode(AppThemeMode.SYSTEM), lastAction.get())
        composeRule.onNodeWithText("Светлая").assertExists()
        composeRule.onNodeWithText("Тёмная").assertExists()
        composeRule.onNodeWithTag("appearance-preview").assertExists()

        composeRule.onNodeWithTag("appearance-settings").performScrollToNode(hasText("Основной текст"))
        composeRule.onNodeWithText("Основной текст").assertExists()
        composeRule.onNodeWithText("Вторичный текст").assertExists()
        composeRule.onNodeWithText("Фон").assertExists()
        composeRule.onNodeWithText("Акцент").assertExists()

        composeRule.onNodeWithTag("appearance-settings")
            .performScrollToNode(hasText("Восстановить стандартную тему"))
        composeRule.onNodeWithText("Восстановить стандартную тему").assertExists().performClick()
        assertEquals(AppearanceUiAction.ResetDefaults, lastAction.get())
        composeRule.onNodeWithText("Плотность интерфейса").assertDoesNotExist()
        composeRule.onNodeWithText("Скругления").assertDoesNotExist()
        composeRule.onNodeWithText("Пользовательский шрифт").assertDoesNotExist()
    }
}

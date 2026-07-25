package io.github.zapolyarnydev.ptktimetable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Group
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.ScheduleMode
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekFilter
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType
import io.github.zapolyarnydev.ptktimetable.feature.catalog.course.CourseScreen
import io.github.zapolyarnydev.ptktimetable.feature.catalog.course.CourseUiAction
import io.github.zapolyarnydev.ptktimetable.feature.catalog.course.CourseUiItem
import io.github.zapolyarnydev.ptktimetable.feature.catalog.course.CourseUiState
import io.github.zapolyarnydev.ptktimetable.feature.catalog.group.GroupScreen
import io.github.zapolyarnydev.ptktimetable.feature.catalog.group.GroupUiAction
import io.github.zapolyarnydev.ptktimetable.feature.catalog.group.GroupUiState
import io.github.zapolyarnydev.ptktimetable.feature.notes.NotesDialogState
import io.github.zapolyarnydev.ptktimetable.feature.notes.NotesUiAction
import io.github.zapolyarnydev.ptktimetable.feature.notes.NotesUiState
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleDataPresentation
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleDay
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleLessonItem
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleScreen
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleUiAction
import io.github.zapolyarnydev.ptktimetable.ui.schedule.ScheduleUiState
import io.github.zapolyarnydev.ptktimetable.ui.schedule.TimeSlotUi
import io.github.zapolyarnydev.ptktimetable.ui.theme.PtkTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.atomic.AtomicBoolean

class PrimaryApplicationFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun courseGroupScheduleNoteReminderFlow() {
        val noteSaved = AtomicBoolean(false)
        val reminderSaved = AtomicBoolean(false)

        composeRule.setContent {
            PtkTheme {
                PrimaryFlowHarness(
                    onNoteSaved = { noteSaved.set(true) },
                    onReminderSaved = { reminderSaved.set(true) },
                )
            }
        }

        composeRule.onNodeWithText("4 курс").performClick()
        composeRule.onNodeWithText("Группа 3991").performClick()
        composeRule.onNodeWithTag("schedule-list").performScrollToNode(hasText("Мобильная разработка"))
        composeRule.onNodeWithText("Мобильная разработка").assertExists()

        composeRule.onNodeWithContentDescription("Добавить заметку").performClick()
        composeRule.onNode(hasSetTextAction()).performTextInput("Подготовить вопросы")
        composeRule.onNodeWithText("Сохранить").performClick()
        composeRule.waitForIdle()
        assertTrue(noteSaved.get())

        composeRule.onNodeWithContentDescription("Напоминание").performClick()
        composeRule.onNode(isToggleable()).performClick()
        composeRule.onNodeWithText("Сохранить").performClick()
        composeRule.waitForIdle()
        assertTrue(reminderSaved.get())
    }
}

@Composable
private fun PrimaryFlowHarness(onNoteSaved: () -> Unit, onReminderSaved: () -> Unit) {
    val group = remember {
        Group(
            collegeName = "Политехнический колледж",
            course = 4,
            courseName = "4 курс",
            groupName = "3991",
            sourceUrl = "https://example.test/3991.xls",
        )
    }
    val lesson = remember {
        ScheduleLessonItem(
            day = ScheduleDay.MONDAY,
            dayLabel = ScheduleDay.MONDAY.title,
            startTime = LocalTime.of(10, 40),
            endTime = LocalTime.of(12, 10),
            weekType = WeekType.ALL,
            subject = "Мобильная разработка",
            teacher = "Иванов И. И.",
            classroom = "312",
            rawText = "Мобильная разработка",
        )
    }
    var destination by remember { mutableStateOf(FlowDestination.COURSES) }
    var notesState by remember {
        mutableStateOf(
            NotesUiState(
                groupName = group.groupName,
                selectedDate = LocalDate.of(2030, 9, 2),
                scheduleMode = ScheduleMode.BY_DATE,
            ),
        )
    }

    when (destination) {
        FlowDestination.COURSES -> CourseScreen(
            state = CourseUiState(
                courses = listOf(CourseUiItem(4, "4 курс")),
                isInitialLoading = false,
                lastUpdatedAt = Instant.parse("2030-09-01T10:00:00Z"),
            ),
            onAction = {
                if (it is CourseUiAction.SelectCourse) destination = FlowDestination.GROUPS
            },
        )

        FlowDestination.GROUPS -> GroupScreen(
            state = GroupUiState(
                courseId = 4,
                courseTitle = "4 курс",
                groups = listOf(group),
                isInitialLoading = false,
                lastUpdatedAt = Instant.parse("2030-09-01T10:00:00Z"),
            ),
            onAction = {
                if (it is GroupUiAction.SelectGroup) destination = FlowDestination.SCHEDULE
            },
        )

        FlowDestination.SCHEDULE -> ScheduleScreen(
            state = scheduleState(group, lesson),
            notesState = notesState,
            onAction = { _: ScheduleUiAction -> },
            onNotesAction = { action ->
                notesState = when (action) {
                    is NotesUiAction.OpenNote -> notesState.copy(
                        dialogs = NotesDialogState(noteLesson = action.lesson),
                        canEditDialog = true,
                    )

                    is NotesUiAction.SaveLessonNote -> {
                        onNoteSaved()
                        notesState.copy(dialogs = NotesDialogState())
                    }

                    is NotesUiAction.OpenReminder -> notesState.copy(
                        dialogs = NotesDialogState(reminderLesson = action.lesson),
                        canEditDialog = true,
                    )

                    is NotesUiAction.SaveReminder -> {
                        onReminderSaved()
                        notesState.copy(dialogs = NotesDialogState())
                    }

                    else -> notesState
                }
            },
        )
    }
}

private fun scheduleState(group: Group, lesson: ScheduleLessonItem): ScheduleUiState {
    val slot = TimeSlotUi(
        startTime = lesson.startTime,
        endTime = lesson.endTime,
        allLessons = listOf(lesson),
        upperLessons = emptyList(),
        lowerLessons = emptyList(),
    )
    return ScheduleUiState(
        selectedGroup = group,
        mode = ScheduleMode.BY_DATE,
        selectedDate = LocalDate.of(2030, 9, 2),
        lessons = listOf(lesson),
        availableDays = listOf(ScheduleDay.MONDAY),
        selectedDay = ScheduleDay.MONDAY,
        weekFilter = WeekFilter.ALL,
        currentWeekType = WeekType.UPPER,
        selectedDateWeekType = WeekType.UPPER,
        scheduleUpdatedAt = Instant.parse("2030-09-01T10:00:00Z"),
        presentation = ScheduleDataPresentation(
            visibleLessons = listOf(lesson),
            timeSlots = listOf(slot),
        ),
    )
}

private enum class FlowDestination {
    COURSES,
    GROUPS,
    SCHEDULE,
}

package io.github.zapolyarnydev.ptktimetable.data.repository

import io.github.zapolyarnydev.ptktimetable.data.model.PtkRawLesson
import io.github.zapolyarnydev.ptktimetable.data.model.PtkWeekType
import io.github.zapolyarnydev.ptktimetable.data.normalize.LessonTextNormalizer
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.LessonOccurrence
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.LessonTemplate
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.RefreshResult
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.TimetableGroup
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekFilter
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType
import io.github.zapolyarnydev.ptktimetable.domain.schedule.repository.TimetableRepository
import io.github.zapolyarnydev.ptktimetable.domain.schedule.service.WeekResolver
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class DomainTimetableRepositoryAdapter(
    private val scheduleRepository: ScheduleRepository = PtkScheduleRepository(),
    private val weekResolver: WeekResolver = PortalBackedWeekResolver(scheduleRepository),
    private val textNormalizer: LessonTextNormalizer = LessonTextNormalizer(),
    private val clock: Clock = Clock.systemDefaultZone()
) : TimetableRepository {

    override suspend fun refreshGroupsAndTemplates(): RefreshResult {
        val groups = scheduleRepository.getGroups()
        runCatching { weekResolver.resolve(LocalDate.now(clock)) }
        return RefreshResult(
            groupsCount = groups.size,
            refreshedAt = Instant.now(clock)
        )
    }

    override suspend fun getGroups(): List<TimetableGroup> {
        return scheduleRepository.getGroups().map { raw ->
            TimetableGroup(
                collegeName = raw.collegeName,
                course = raw.course,
                courseName = raw.courseName,
                groupName = raw.groupName,
                sourceUrl = raw.xlsUrl
            )
        }
    }

    override suspend fun getTemplatesByGroup(groupName: String): List<LessonTemplate> {
        val sourceUpdatedAt = Instant.now(clock)
        return scheduleRepository.getScheduleForGroup(groupName)
            .mapNotNull { raw ->
                val dayOfWeek = parseDayOfWeek(raw.dayOfWeek) ?: return@mapNotNull null
                val (startTime, endTime) = parseTimeRange(raw.timeRange)
                val normalized = textNormalizer.normalize(raw.rawText)
                val weekType = raw.weekType.toDomainWeekType()
                val safeSubject = normalized.subject.ifBlank { raw.rawText.trim() }

                LessonTemplate(
                    id = buildTemplateId(raw, dayOfWeek, startTime, endTime),
                    groupName = raw.groupName,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    weekType = weekType,
                    subject = safeSubject,
                    teacher = normalized.teacher,
                    room = normalized.classroom,
                    rawText = raw.rawText,
                    sourceUpdatedAt = sourceUpdatedAt
                )
            }
            .sortedWith(compareBy<LessonTemplate> { it.dayOfWeek.value }.thenBy { it.startTime })
    }

    override suspend fun getOccurrencesByDate(
        groupName: String,
        date: LocalDate
    ): List<LessonOccurrence> {
        val templates = getTemplatesByGroup(groupName)
            .filter { it.dayOfWeek == date.dayOfWeek }
        val weekInfo = weekResolver.resolve(date)
        val filteredTemplates = templates.filter { template ->
            matchesWeek(template.weekType, weekInfo.isUpper)
        }

        return filteredTemplates
            .map { template -> template.toOccurrence(date) }
            .sortedBy { it.startDateTime }
    }

    override suspend fun getOccurrencesByDayOfWeek(
        groupName: String,
        dayOfWeek: DayOfWeek,
        weekFilter: WeekFilter,
        anchorDate: LocalDate
    ): List<LessonOccurrence> {
        val targetDate = anchorDate
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .plusDays((dayOfWeek.value - 1).toLong())

        val templates = getTemplatesByGroup(groupName)
            .filter { it.dayOfWeek == dayOfWeek }
            .filter { template ->
                when (weekFilter) {
                    WeekFilter.ALL -> true
                    WeekFilter.UPPER -> template.weekType == WeekType.ALL || template.weekType == WeekType.UPPER
                    WeekFilter.LOWER -> template.weekType == WeekType.ALL || template.weekType == WeekType.LOWER
                }
            }

        return templates
            .map { template -> template.toOccurrence(targetDate) }
            .sortedBy { it.startDateTime }
    }

    private fun parseDayOfWeek(rawValue: String): DayOfWeek? {
        val normalized = rawValue
            .trim()
            .lowercase(Locale.ROOT)
            .replace('ё', 'е')
        return when {
            normalized.contains("понедельник") || normalized == "пн" -> DayOfWeek.MONDAY
            normalized.contains("вторник") || normalized == "вт" -> DayOfWeek.TUESDAY
            normalized.contains("среда") || normalized == "ср" -> DayOfWeek.WEDNESDAY
            normalized.contains("четверг") || normalized == "чт" -> DayOfWeek.THURSDAY
            normalized.contains("пятница") || normalized == "пт" -> DayOfWeek.FRIDAY
            normalized.contains("суббота") || normalized == "сб" -> DayOfWeek.SATURDAY
            normalized.contains("воскресенье") || normalized == "вс" -> DayOfWeek.SUNDAY
            else -> null
        }
    }

    private fun parseTimeRange(rawValue: String): Pair<LocalTime, LocalTime> {
        val normalized = rawValue.replace('—', '-').replace('–', '-')
        val matches = TIME_REGEX.findAll(normalized).toList()
        val start = matches.firstOrNull()?.toLocalTime() ?: LocalTime.of(0, 0)
        val end = matches.getOrNull(1)?.toLocalTime() ?: start.plusMinutes(DEFAULT_LESSON_MINUTES)
        return start to end
    }

    private fun kotlin.text.MatchResult.toLocalTime(): LocalTime {
        val hours = groupValues.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 23) ?: 0
        val minutes = groupValues.getOrNull(2)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        return LocalTime.of(hours, minutes)
    }

    private fun buildTemplateId(
        raw: PtkRawLesson,
        dayOfWeek: DayOfWeek,
        startTime: LocalTime,
        endTime: LocalTime
    ): String {
        val payload = listOf(
            raw.groupName.trim(),
            dayOfWeek.name,
            startTime.toString(),
            endTime.toString(),
            raw.weekType.name,
            raw.rawText.trim()
        ).joinToString("|")
        return payload.hashCode().toUInt().toString(16)
    }

    private fun matchesWeek(weekType: WeekType, isUpper: Boolean?): Boolean {
        if (weekType == WeekType.ALL) return true
        if (isUpper == null) return true
        return if (isUpper) {
            weekType == WeekType.UPPER
        } else {
            weekType == WeekType.LOWER
        }
    }

    private fun LessonTemplate.toOccurrence(date: LocalDate): LessonOccurrence {
        return LessonOccurrence(
            templateId = id,
            groupName = groupName,
            date = date,
            startDateTime = LocalDateTime.of(date, startTime),
            endDateTime = LocalDateTime.of(date, endTime),
            weekTypeResolved = weekType,
            subject = subject,
            teacher = teacher,
            room = room,
            rawText = rawText
        )
    }

    private fun PtkWeekType.toDomainWeekType(): WeekType {
        return when (this) {
            PtkWeekType.ALL -> WeekType.ALL
            PtkWeekType.UPPER -> WeekType.UPPER
            PtkWeekType.LOWER -> WeekType.LOWER
        }
    }

    private companion object {
        val TIME_REGEX = Regex("(\\d{1,2})[.:](\\d{2})")
        const val DEFAULT_LESSON_MINUTES = 100L
    }
}

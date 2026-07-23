package io.github.zapolyarnydev.ptktimetable.data.mapper

import io.github.zapolyarnydev.ptktimetable.data.normalize.LessonTextNormalizer
import io.github.zapolyarnydev.ptktimetable.data.remote.xls.NovsuRawLesson
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.Lesson
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.util.Locale

class NovsuLessonMapper(
    private val textNormalizer: LessonTextNormalizer = LessonTextNormalizer(),
    private val clock: Clock = Clock.systemDefaultZone(),
) {

    fun map(rawLessons: List<NovsuRawLesson>): List<Lesson> {
        val sourceUpdatedAt = Instant.now(clock)
        return rawLessons
            .mapNotNull { raw -> raw.toLesson(sourceUpdatedAt) }
            .sortedWith(compareBy<Lesson> { it.dayOfWeek.value }.thenBy { it.startTime })
    }

    private fun NovsuRawLesson.toLesson(sourceUpdatedAt: Instant): Lesson? {
        val parsedDay = parseDayOfWeek(dayOfWeek) ?: return null
        val (parsedStart, parsedEnd) = parseTimeRange(timeRange, parsedDay)
        val normalized = textNormalizer.normalize(rawText)
        return Lesson(
            id = buildLessonId(this, parsedDay, parsedStart, parsedEnd),
            groupName = groupName,
            dayOfWeek = parsedDay,
            startTime = parsedStart,
            endTime = parsedEnd,
            weekType = weekType,
            subject = normalized.subject.ifBlank { rawText.trim() },
            teacher = normalized.teacher,
            room = normalized.classroom,
            rawText = rawText,
            sourceUpdatedAt = sourceUpdatedAt,
        )
    }

    private fun parseDayOfWeek(rawValue: String): DayOfWeek? {
        val normalized = rawValue.trim().lowercase(Locale.ROOT).replace('ё', 'е')
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

    private fun parseTimeRange(rawValue: String, dayOfWeek: DayOfWeek): Pair<LocalTime, LocalTime> {
        saturdayOverride(rawValue, dayOfWeek)?.let { return it }
        val matches = TIME_REGEX.findAll(rawValue.replace('—', '-').replace('–', '-')).toList()
        val start = matches.firstOrNull()?.toLocalTime() ?: LocalTime.MIDNIGHT
        val end = matches.getOrNull(1)?.toLocalTime() ?: start.plusMinutes(DEFAULT_LESSON_MINUTES)
        return start to end
    }

    private fun saturdayOverride(rawValue: String, dayOfWeek: DayOfWeek): Pair<LocalTime, LocalTime>? {
        if (dayOfWeek != DayOfWeek.SATURDAY) return null
        val key = rawValue
            .trim()
            .replace('—', '-')
            .replace('–', '-')
            .replace(':', '.')
            .replace(" ", "")
        return SATURDAY_TIME_OVERRIDES[key]
    }

    private fun MatchResult.toLocalTime(): LocalTime {
        val hours = groupValues.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 23) ?: 0
        val minutes = groupValues.getOrNull(2)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        return LocalTime.of(hours, minutes)
    }

    private fun buildLessonId(
        raw: NovsuRawLesson,
        dayOfWeek: DayOfWeek,
        startTime: LocalTime,
        endTime: LocalTime,
    ): String {
        val payload = listOf(
            raw.groupName.trim(),
            dayOfWeek.name,
            startTime.toString(),
            endTime.toString(),
            raw.weekType.name,
            raw.rawText.trim(),
        ).joinToString("|")
        return payload.hashCode().toUInt().toString(16)
    }

    private companion object {
        val TIME_REGEX = Regex("(\\d{1,2})[.:](\\d{2})")
        const val DEFAULT_LESSON_MINUTES = 100L
        val SATURDAY_TIME_OVERRIDES = mapOf(
            "8.30-10.10" to (LocalTime.of(8, 30) to LocalTime.of(9, 30)),
            "10.20-12.00" to (LocalTime.of(9, 40) to LocalTime.of(10, 40)),
            "12.45-14.25" to (LocalTime.of(10, 50) to LocalTime.of(11, 50)),
            "14.35-16.15" to (LocalTime.of(12, 0) to LocalTime.of(13, 0)),
            "16.25-18.05" to (LocalTime.of(13, 10) to LocalTime.of(14, 10)),
        )
    }
}

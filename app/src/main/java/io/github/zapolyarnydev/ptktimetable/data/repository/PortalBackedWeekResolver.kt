package io.github.zapolyarnydev.ptktimetable.data.repository

import io.github.zapolyarnydev.ptktimetable.data.model.PtkCurrentWeekType
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekInfo
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekSource
import io.github.zapolyarnydev.ptktimetable.domain.schedule.service.WeekResolver
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.abs

class PortalBackedWeekResolver(
    private val scheduleRepository: ScheduleRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val fallbackReferenceDate: LocalDate = LocalDate.of(2025, 9, 1),
    private val fallbackReferenceIsUpper: Boolean = true
) : WeekResolver {

    override suspend fun resolve(date: LocalDate): WeekInfo {
        val weekForDate = runCatching { scheduleRepository.getWeekTypeForDate(date) }
            .getOrDefault(PtkCurrentWeekType.UNKNOWN)

        when (weekForDate) {
            PtkCurrentWeekType.UPPER -> {
                return WeekInfo(
                    date = date,
                    isUpper = true,
                    source = WeekSource.PORTAL
                )
            }

            PtkCurrentWeekType.LOWER -> {
                return WeekInfo(
                    date = date,
                    isUpper = false,
                    source = WeekSource.PORTAL
                )
            }

            PtkCurrentWeekType.UNKNOWN -> Unit
        }

        val currentType = runCatching { scheduleRepository.getCurrentWeekType() }
            .getOrDefault(PtkCurrentWeekType.UNKNOWN)

        return when (currentType) {
            PtkCurrentWeekType.UPPER -> WeekInfo(
                date = date,
                isUpper = resolveByParity(date, isUpperAtAnchor = true, anchorDate = LocalDate.now(clock)),
                source = WeekSource.PORTAL
            )

            PtkCurrentWeekType.LOWER -> WeekInfo(
                date = date,
                isUpper = resolveByParity(date, isUpperAtAnchor = false, anchorDate = LocalDate.now(clock)),
                source = WeekSource.PORTAL
            )

            PtkCurrentWeekType.UNKNOWN -> WeekInfo(
                date = date,
                isUpper = resolveByParity(
                    date = date,
                    isUpperAtAnchor = fallbackReferenceIsUpper,
                    anchorDate = fallbackReferenceDate
                ),
                source = WeekSource.LOCAL_RULE
            )
        }
    }

    override suspend fun resolveRange(from: LocalDate, to: LocalDate): Map<LocalDate, WeekInfo> {
        if (from == to) return mapOf(from to resolve(from))
        val start = minOf(from, to)
        val end = maxOf(from, to)
        val result = LinkedHashMap<LocalDate, WeekInfo>()
        var cursor = start
        while (!cursor.isAfter(end)) {
            result[cursor] = resolve(cursor)
            cursor = cursor.plusDays(1)
        }
        return result
    }

    private fun resolveByParity(
        date: LocalDate,
        isUpperAtAnchor: Boolean,
        anchorDate: LocalDate
    ): Boolean {
        val anchorWeekStart = anchorDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val targetWeekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weeksDiff = ChronoUnit.WEEKS.between(anchorWeekStart, targetWeekStart)
        val isOddShift = abs(weeksDiff % 2L) == 1L
        return if (isOddShift) !isUpperAtAnchor else isUpperAtAnchor
    }
}

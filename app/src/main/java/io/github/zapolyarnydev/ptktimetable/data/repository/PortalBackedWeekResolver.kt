package io.github.zapolyarnydev.ptktimetable.data.repository

import io.github.zapolyarnydev.ptktimetable.data.remote.NovsuScheduleDataSource
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekInfo
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekSource
import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType
import io.github.zapolyarnydev.ptktimetable.domain.schedule.service.WeekResolver
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.abs

class PortalBackedWeekResolver(
    private val scheduleDataSource: NovsuScheduleDataSource,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val fallbackReferenceDate: LocalDate = LocalDate.of(2025, 9, 1),
    private val fallbackReferenceIsUpper: Boolean = true,
) : WeekResolver {

    override suspend fun resolve(date: LocalDate): WeekInfo {
        val weekForDate = runCatching { scheduleDataSource.getWeekTypeForDate(date) }
            .getOrNull()

        when (weekForDate) {
            WeekType.UPPER -> {
                return WeekInfo(
                    date = date,
                    weekType = WeekType.UPPER,
                    source = WeekSource.PORTAL,
                )
            }

            WeekType.LOWER -> {
                return WeekInfo(
                    date = date,
                    weekType = WeekType.LOWER,
                    source = WeekSource.PORTAL,
                )
            }

            WeekType.ALL, null -> Unit
        }

        val currentType = runCatching { scheduleDataSource.getCurrentWeekType() }
            .getOrNull()

        return when (currentType) {
            WeekType.UPPER -> WeekInfo(
                date = date,
                weekType = resolveByParity(
                    date,
                    isUpperAtAnchor = true,
                    anchorDate = LocalDate.now(clock),
                ).toWeekType(),
                source = WeekSource.PORTAL,
            )

            WeekType.LOWER -> WeekInfo(
                date = date,
                weekType = resolveByParity(
                    date,
                    isUpperAtAnchor = false,
                    anchorDate = LocalDate.now(clock),
                ).toWeekType(),
                source = WeekSource.PORTAL,
            )

            WeekType.ALL, null -> WeekInfo(
                date = date,
                weekType = resolveByParity(
                    date = date,
                    isUpperAtAnchor = fallbackReferenceIsUpper,
                    anchorDate = fallbackReferenceDate,
                ).toWeekType(),
                source = WeekSource.LOCAL_RULE,
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

    private fun resolveByParity(date: LocalDate, isUpperAtAnchor: Boolean, anchorDate: LocalDate): Boolean {
        val anchorWeekStart = anchorDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val targetWeekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weeksDiff = ChronoUnit.WEEKS.between(anchorWeekStart, targetWeekStart)
        val isOddShift = abs(weeksDiff % 2L) == 1L
        return if (isOddShift) !isUpperAtAnchor else isUpperAtAnchor
    }

    private fun Boolean.toWeekType(): WeekType = if (this) WeekType.UPPER else WeekType.LOWER
}

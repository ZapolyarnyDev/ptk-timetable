package io.github.zapolyarnydev.ptktimetable.core.ui

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun formatDateTitle(date: LocalDate): String =
    DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("ru")).format(date)

internal fun formatInstant(value: Instant): String =
    DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.forLanguageTag("ru"))
        .format(value.atZone(ZoneId.systemDefault()))

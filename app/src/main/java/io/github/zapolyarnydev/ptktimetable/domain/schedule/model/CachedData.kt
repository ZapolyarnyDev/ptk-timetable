package io.github.zapolyarnydev.ptktimetable.domain.schedule.model

import java.time.Instant

data class CachedData<out T>(val data: T, val updatedAt: Instant?)

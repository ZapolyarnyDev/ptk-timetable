package io.github.zapolyarnydev.ptktimetable.domain.schedule.model

import java.time.Instant

data class RefreshResult(
    val groupsCount: Int,
    val refreshedAt: Instant
)


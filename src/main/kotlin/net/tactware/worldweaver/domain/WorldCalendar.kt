package net.tactware.worldweaver.domain

import java.time.Instant

internal data class WorldCalendar(
    val id: String,
    val worldId: String,
    val eraSuffix: String,
    val months: List<WorldCalendarMonth>,
    val weekdays: List<WorldCalendarWeekday>,
    val currentDate: WorldDate?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

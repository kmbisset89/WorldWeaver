package io.github.kmbisset89.worldweaver.domain

internal data class WorldCalendarDraft(
    val eraSuffix: String,
    val months: List<WorldCalendarMonth>,
    val weekdays: List<WorldCalendarWeekday>,
    val currentDate: WorldDate?,
)

package io.github.kmbisset89.worldweaver.domain

import java.time.Instant

internal data class WorldCalendarObservance(
    val id: String,
    val worldId: String,
    val name: String,
    val notes: String,
    val kind: WorldCalendarObservanceKind,
    val monthId: String,
    val day: Int,
    val year: Int?,
    val loreIds: List<String>,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    /**
     * Whether this observance falls on [date].
     *
     * A missing [year] matches every year on the same month and day.
     */
    fun matches(date: WorldDate): Boolean {
        if (monthId != date.monthId || day != date.day) {
            return false
        }
        return year == null || year == date.year
    }
}

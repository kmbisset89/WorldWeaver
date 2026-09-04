package io.github.kmbisset89.worldweaver.domain

internal data class WorldCalendarObservanceDraft(
    val name: String,
    val notes: String,
    val kind: WorldCalendarObservanceKind,
    val monthId: String,
    val day: Int,
    val year: Int?,
    val loreIds: List<String>,
)

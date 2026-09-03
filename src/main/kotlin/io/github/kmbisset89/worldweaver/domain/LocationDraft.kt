package io.github.kmbisset89.worldweaver.domain

internal data class LocationDraft(
    val type: LocationType,
    val parentLocationId: String?,
    val name: String,
    val description: String,
    val climate: String,
    val terrain: String,
    val government: String,
    val landmarks: List<String>,
    val history: String,
    val notes: String,
)

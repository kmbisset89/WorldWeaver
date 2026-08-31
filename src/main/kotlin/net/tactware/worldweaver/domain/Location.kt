package net.tactware.worldweaver.domain

import java.time.Instant

internal data class Location(
    val id: String,
    val worldId: String,
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
    val createdAt: Instant,
    val updatedAt: Instant,
)

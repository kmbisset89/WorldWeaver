package io.github.kmbisset89.worldweaver.domain

import java.time.Instant

internal data class WorldCelestialBody(
    val id: String,
    val worldId: String,
    val name: String,
    val notes: String,
    val kind: CelestialBodyKind,
    val periodDays: Int,
    val epochOffsetDays: Int,
    val sortIndex: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)

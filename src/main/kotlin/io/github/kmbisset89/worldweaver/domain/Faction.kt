package io.github.kmbisset89.worldweaver.domain

import java.time.Instant

internal data class Faction(
    val id: String,
    val worldId: String,
    val name: String,
    val description: String,
    val goals: String,
    val notes: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)

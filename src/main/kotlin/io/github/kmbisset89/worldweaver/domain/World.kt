package io.github.kmbisset89.worldweaver.domain

import java.time.Instant

internal data class World(
    val id: String,
    val name: String,
    val description: String,
    val defaultGameSystem: GameSystem,
    val createdAt: Instant,
    val updatedAt: Instant,
)

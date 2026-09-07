package io.github.kmbisset89.worldweaver.domain

import java.time.Instant

internal data class RandomTable(
    val id: String,
    val worldId: String,
    val name: String,
    val notes: String,
    val rows: List<RandomTableRow>,
    val createdAt: Instant,
    val updatedAt: Instant,
)

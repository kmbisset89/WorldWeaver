package net.tactware.worldweaver.domain

import java.time.Instant

internal data class Lore(
    val id: String,
    val worldId: String,
    val title: String,
    val content: String,
    val category: LoreCategory,
    val tags: List<String>,
    val relatedEntryIds: List<String>,
    val secrets: List<LoreSecret>,
    val locationId: String?,
    val characterId: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

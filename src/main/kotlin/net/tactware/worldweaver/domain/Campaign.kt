package net.tactware.worldweaver.domain

import java.time.Instant

internal data class Campaign(
    val id: String,
    val worldId: String,
    val name: String,
    val description: String,
    val notes: String,
    val gameSystem: GameSystem?,
    val status: CampaignStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
)

package net.tactware.worldweaver.domain

import java.time.Instant

internal data class Quest(
    val id: String,
    val campaignId: String,
    val title: String,
    val summary: String,
    val status: QuestStatus,
    val locationId: String?,
    val objectives: List<QuestObjective>,
    val links: List<QuestLink>,
    val createdAt: Instant,
    val updatedAt: Instant,
)

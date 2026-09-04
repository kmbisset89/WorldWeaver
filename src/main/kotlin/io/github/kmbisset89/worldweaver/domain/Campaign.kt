package io.github.kmbisset89.worldweaver.domain

import java.time.Instant

internal data class Campaign(
    val id: String,
    val worldId: String,
    val name: String,
    val description: String,
    val notes: String,
    val gameSystem: GameSystem?,
    val levelingMode: LevelingMode = LevelingMode.Milestone,
    val status: CampaignStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    fun resolvedGameSystem(worldDefault: GameSystem): GameSystem {
        return GameSystem.resolve(gameSystem, worldDefault)
    }
}

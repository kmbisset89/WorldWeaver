package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals

internal class GameSystemTest {
    @Test
    fun resolveUsesStoredCampaignSystem() {
        assertEquals(
            GameSystem.Pathfinder2E,
            GameSystem.resolve(GameSystem.Pathfinder2E, GameSystem.FifthEdition),
        )
    }

    @Test
    fun resolveFallsBackToWorldDefaultWhenCampaignHasNone() {
        assertEquals(
            GameSystem.Pathfinder2E,
            GameSystem.resolve(null, GameSystem.Pathfinder2E),
        )
        assertEquals(
            GameSystem.FifthEdition,
            GameSystem.resolve(null, GameSystem.FifthEdition),
        )
    }

    @Test
    fun campaignResolvedGameSystemUsesWorldDefaultWhenNull() {
        val campaign = Campaign(
            id = "c-1",
            worldId = "w-1",
            name = "Test",
            description = "",
            notes = "",
            gameSystem = null,
            status = CampaignStatus.Active,
            createdAt = java.time.Instant.parse("2026-08-31T12:00:00Z"),
            updatedAt = java.time.Instant.parse("2026-08-31T12:00:00Z"),
        )

        assertEquals(
            GameSystem.Pathfinder2E,
            campaign.resolvedGameSystem(GameSystem.Pathfinder2E),
        )
    }
}

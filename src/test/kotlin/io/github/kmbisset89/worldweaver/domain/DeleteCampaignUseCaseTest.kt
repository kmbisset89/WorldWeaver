package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class DeleteCampaignUseCaseTest {
    @Test
    fun deleteRemovesCampaignAndLeavesWorld() = runTest {
        val harness = Harness()
        val world = harness.insertWorld()
        val campaign = harness.insertCampaign(world.id)
        harness.context.setActiveWorldId(world.id)
        harness.context.setActiveCampaignId(campaign.id)

        harness.deleteCampaign(campaign.id)

        assertTrue(harness.campaigns.all().isEmpty())
        assertNotNull(harness.worlds.getById(world.id))
        assertEquals(world.id, harness.context.get().activeWorldId)
        assertNull(harness.context.get().activeCampaignId)
    }

    private class Harness {
        val worlds = FakeWorldRepository()
        val campaigns = FakeCampaignRepository()
        val context = FakeActiveContextRepository()
        private val now = Instant.parse("2026-08-29T12:00:00Z")
        val deleteCampaign = DeleteCampaignUseCase(campaigns, context)

        suspend fun insertWorld(): World {
            val world = World(
                id = "world-1",
                name = "Faerun",
                description = "",
                defaultGameSystem = GameSystem.FifthEdition,
                createdAt = now,
                updatedAt = now,
            )
            worlds.insert(world)
            return world
        }

        suspend fun insertCampaign(worldId: String): Campaign {
            val campaign = Campaign(
                id = "campaign-1",
                worldId = worldId,
                name = "Icewind Dale",
                description = "",
                notes = "",
                gameSystem = null,
                status = CampaignStatus.Active,
                createdAt = now,
                updatedAt = now,
            )
            campaigns.insert(campaign)
            return campaign
        }
    }
}

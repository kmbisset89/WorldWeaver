package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class DeleteWorldUseCaseTest {
    @Test
    fun deleteIsBlockedWhenCampaignsExist() = runTest {
        val harness = Harness()
        val world = harness.insertWorld("Faerun")
        harness.insertCampaign(world.id, "Icewind Dale")

        val result = harness.deleteWorld(world.id)

        val blocked = assertIs<DeleteWorldUseCase.Result.Blocked>(result)
        assertEquals(1, blocked.campaignCount)
        assertEquals(world.id, harness.worlds.getById(world.id)?.id)
        assertEquals(1, harness.campaigns.all().size)
    }

    @Test
    fun deleteRemovesWorldWithoutCampaignsAndClearsContext() = runTest {
        val harness = Harness()
        val world = harness.insertWorld("Faerun")
        harness.context.setActiveWorldId(world.id)

        val result = harness.deleteWorld(world.id)

        assertIs<DeleteWorldUseCase.Result.Deleted>(result)
        assertTrue(harness.worlds.all().isEmpty())
        assertNull(harness.context.get().activeWorldId)
        assertNull(harness.context.get().activeCampaignId)
    }

    private class Harness {
        val worlds = FakeWorldRepository()
        val campaigns = FakeCampaignRepository()
        val context = FakeActiveContextRepository()
        private val now = Instant.parse("2026-08-29T12:00:00Z")
        val deleteWorld = DeleteWorldUseCase(worlds, campaigns, context)

        suspend fun insertWorld(name: String): World {
            val world = World(
                id = "world-1",
                name = name,
                description = "",
                defaultGameSystem = GameSystem.FifthEdition,
                createdAt = now,
                updatedAt = now,
            )
            worlds.insert(world)
            return world
        }

        suspend fun insertCampaign(worldId: String, name: String): Campaign {
            val campaign = Campaign(
                id = "campaign-1",
                worldId = worldId,
                name = name,
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

package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class SetActiveContextUseCaseTest {
    @Test
    fun setActiveCampaignAlsoSetsParentWorld() = runTest {
        val harness = Harness()
        val world = harness.insertWorld("world-1", "Faerun")
        val campaign = harness.insertCampaign("campaign-1", world.id)

        harness.setActiveCampaign(campaign.id)

        assertEquals(world.id, harness.context.get().activeWorldId)
        assertEquals(campaign.id, harness.context.get().activeCampaignId)
    }

    @Test
    fun clearActiveCampaignKeepsWorld() = runTest {
        val harness = Harness()
        val world = harness.insertWorld("world-1", "Faerun")
        val campaign = harness.insertCampaign("campaign-1", world.id)
        harness.setActiveCampaign(campaign.id)

        harness.clearActiveCampaign()

        assertEquals(world.id, harness.context.get().activeWorldId)
        assertNull(harness.context.get().activeCampaignId)
    }

    @Test
    fun setActiveWorldDropsCampaignFromAnotherWorld() = runTest {
        val harness = Harness()
        val first = harness.insertWorld("world-1", "Faerun")
        val second = harness.insertWorld("world-2", "Eberron")
        val campaign = harness.insertCampaign("campaign-1", first.id)
        harness.setActiveCampaign(campaign.id)

        harness.setActiveWorld(second.id)

        assertEquals(second.id, harness.context.get().activeWorldId)
        assertNull(harness.context.get().activeCampaignId)
    }

    @Test
    fun setActiveWorldKeepsCampaignInSameWorld() = runTest {
        val harness = Harness()
        val world = harness.insertWorld("world-1", "Faerun")
        val campaign = harness.insertCampaign("campaign-1", world.id)
        harness.setActiveCampaign(campaign.id)

        harness.setActiveWorld(world.id)

        assertEquals(world.id, harness.context.get().activeWorldId)
        assertEquals(campaign.id, harness.context.get().activeCampaignId)
    }

    @Test
    fun setActiveCampaignClearsSessionFromPreviousCampaign() = runTest {
        val harness = Harness()
        val world = harness.insertWorld("world-1", "Faerun")
        val first = harness.insertCampaign("campaign-1", world.id)
        val second = harness.insertCampaign("campaign-2", world.id)
        harness.setActiveCampaign(first.id)
        harness.context.setActiveSessionId("session-1")

        harness.setActiveCampaign(second.id)

        assertEquals(second.id, harness.context.get().activeCampaignId)
        assertNull(harness.context.get().activeSessionId)
    }

    private class Harness {
        val worlds = FakeWorldRepository()
        val campaigns = FakeCampaignRepository()
        val context = FakeActiveContextRepository()
        private val now = Instant.parse("2026-08-29T12:00:00Z")
        private val instant = InstantProvider { now }
        val setActiveWorld = SetActiveWorldUseCase(worlds, campaigns, context, instant)
        val setActiveCampaign = SetActiveCampaignUseCase(campaigns, context)
        val clearActiveCampaign = ClearActiveCampaignUseCase(context)

        suspend fun insertWorld(id: String, name: String): World {
            val world = World(
                id = id,
                name = name,
                description = "",
                defaultGameSystem = GameSystem.FifthEdition,
                createdAt = now,
                updatedAt = now,
            )
            worlds.insert(world)
            return world
        }

        suspend fun insertCampaign(id: String, worldId: String): Campaign {
            val campaign = Campaign(
                id = id,
                worldId = worldId,
                name = "Campaign",
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

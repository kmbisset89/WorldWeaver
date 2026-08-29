package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class CreateCampaignUseCaseTest {
    @Test
    fun createRequiresActiveWorld() = runTest {
        val harness = Harness()

        val result = harness.createCampaign("Icewind Dale", "", "")

        assertIs<CreateCampaignUseCase.Result.NoActiveWorld>(result)
        assertTrue(harness.campaigns.all().isEmpty())
    }

    @Test
    fun createRejectsBlankName() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")

        val result = harness.createCampaign("  ", "", "")

        assertIs<CreateCampaignUseCase.Result.InvalidName>(result)
    }

    @Test
    fun createInheritsWorldMechanicsAndSetsActive() = runTest {
        val harness = Harness()
        val world = World(
            id = "world-1",
            name = "Faerun",
            description = "",
            defaultGameSystem = GameSystem.FifthEdition,
            createdAt = Instant.parse("2026-08-29T12:00:00Z"),
            updatedAt = Instant.parse("2026-08-29T12:00:00Z"),
        )
        harness.worlds.insert(world)
        harness.context.setActiveWorldId(world.id)

        val result = harness.createCampaign("Icewind Dale", "North", "Notes")

        val created = assertIs<CreateCampaignUseCase.Result.Created>(result)
        assertEquals(world.id, created.campaign.worldId)
        assertNull(created.campaign.gameSystem)
        assertEquals(CampaignStatus.Active, created.campaign.status)
        assertEquals(created.campaign.id, harness.context.get().activeCampaignId)
        assertEquals(world.id, harness.context.get().activeWorldId)
    }

    private class Harness {
        val worlds = FakeWorldRepository()
        val campaigns = FakeCampaignRepository()
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        private var nextId = 0
        private val ids = EntityIdFactory { "campaign-${++nextId}" }
        private val setActiveCampaign = SetActiveCampaignUseCase(campaigns, context)
        val createCampaign = CreateCampaignUseCase(
            campaigns,
            context,
            ids,
            instant,
            setActiveCampaign,
        )
    }
}

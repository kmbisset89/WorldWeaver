package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class SetActiveSessionUseCaseTest {
    @Test
    fun setActiveSessionAlsoSetsCampaignAndWorld() = runTest {
        val harness = Harness()
        val world = harness.insertWorld()
        val campaign = harness.insertCampaign(world.id)
        val session = harness.insertSession(campaign.id)

        harness.setActiveSession(session.id)

        assertEquals(world.id, harness.context.get().activeWorldId)
        assertEquals(campaign.id, harness.context.get().activeCampaignId)
        assertEquals(session.id, harness.context.get().activeSessionId)
    }

    @Test
    fun clearActiveSessionLeavesCampaign() = runTest {
        val harness = Harness()
        val world = harness.insertWorld()
        val campaign = harness.insertCampaign(world.id)
        val session = harness.insertSession(campaign.id)
        harness.setActiveSession(session.id)

        harness.setActiveSession(null)

        assertEquals(campaign.id, harness.context.get().activeCampaignId)
        assertNull(harness.context.get().activeSessionId)
    }

    private class Harness {
        val worlds = FakeWorldRepository()
        val campaigns = FakeCampaignRepository()
        val sessions = FakeSessionRepository()
        val context = FakeActiveContextRepository()
        private val now = Instant.parse("2026-08-29T12:00:00Z")
        val setActiveSession = SetActiveSessionUseCase(sessions, campaigns, context)

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
                name = "Heist",
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

        suspend fun insertSession(campaignId: String): Session {
            val session = Session(
                id = "session-1",
                campaignId = campaignId,
                name = "Session 1",
                notes = "",
                scenes = emptyList(),
                marchOrder = emptyList(),
                createdAt = now,
                updatedAt = now,
            )
            sessions.insert(session)
            return session
        }
    }
}

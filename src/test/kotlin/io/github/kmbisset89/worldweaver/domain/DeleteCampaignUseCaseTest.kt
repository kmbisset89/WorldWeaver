package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
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

    @Test
    fun deleteRemovesSessionRecordings() = runTest {
        val harness = Harness()
        val world = harness.insertWorld()
        val campaign = harness.insertCampaign(world.id)
        val session = Session(
            id = "session-1",
            campaignId = campaign.id,
            name = "Tonight",
            notes = "",
            scenes = emptyList(),
            marchOrder = emptyList(),
            createdAt = Instant.parse("2026-08-29T12:00:00Z"),
            updatedAt = Instant.parse("2026-08-29T12:00:00Z"),
        )
        harness.sessions.insert(session)
        val file = harness.recordings.createFile(
            session.id,
            SessionRecordingKind.Video,
            Instant.parse("2026-09-06T15:00:00Z"),
        )
        file.writeBytes(ByteArray(16))

        harness.deleteCampaign(campaign.id)

        assertTrue(harness.recordings.list(session.id).isEmpty())
        assertTrue(!file.exists())
    }

    private class Harness {
        val worlds = FakeWorldRepository()
        val campaigns = FakeCampaignRepository()
        val sessions = FakeSessionRepository()
        val recordings = SessionRecordingFileStore(Files.createTempDirectory("ww-recordings").toFile())
        val context = FakeActiveContextRepository()
        private val now = Instant.parse("2026-08-29T12:00:00Z")
        val deleteCampaign = DeleteCampaignUseCase(campaigns, sessions, recordings, context)

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

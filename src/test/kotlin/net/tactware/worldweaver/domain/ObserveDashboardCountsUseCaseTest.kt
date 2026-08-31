package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ObserveDashboardCountsUseCaseTest {
    @Test
    fun countsWorldsCampaignsAndPeople() = runTest {
        val worlds = FakeWorldRepository()
        val campaigns = FakeCampaignRepository()
        val worldPeople = FakeWorldPersonRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        val now = Instant.parse("2026-08-29T12:00:00Z")
        worlds.insert(
            World("w-1", "Faerun", "", GameSystem.FifthEdition, now, now),
        )
        worlds.insert(
            World("w-2", "Eberron", "", GameSystem.FifthEdition, now, now),
        )
        campaigns.insert(
            Campaign(
                id = "c-1",
                worldId = "w-1",
                name = "Icewind Dale",
                description = "",
                notes = "",
                gameSystem = null,
                status = CampaignStatus.Active,
                createdAt = now,
                updatedAt = now,
            ),
        )
        worldPeople.insert(
            WorldPerson(
                id = "wp-1",
                worldId = "w-1",
                kind = PersonKind.Npc,
                name = "Bram",
                description = "",
                sheet = FifthEditionSheet.empty(),
                createdAt = now,
                updatedAt = now,
            ),
        )
        campaignPeople.insert(
            CampaignPerson(
                id = "cp-1",
                campaignId = "c-1",
                worldPersonId = null,
                kind = PersonKind.PlayerCharacter,
                name = "Aelar",
                description = "",
                sheet = FifthEditionSheet.empty(),
                overlayHitPoints = null,
                overlayNotes = "",
                createdAt = now,
                updatedAt = now,
            ),
        )
        campaignPeople.insert(
            CampaignPerson(
                id = "cp-2",
                campaignId = "c-1",
                worldPersonId = null,
                kind = PersonKind.Npc,
                name = "Innkeep",
                description = "",
                sheet = FifthEditionSheet.empty(),
                overlayHitPoints = null,
                overlayNotes = "",
                createdAt = now,
                updatedAt = now,
            ),
        )

        val counts = ObserveDashboardCountsUseCase(
            worlds,
            campaigns,
            worldPeople,
            campaignPeople,
        )().first()

        assertEquals(DashboardCounts(worlds = 2, campaigns = 1, people = 3), counts)
    }
}

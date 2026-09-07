package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ListWikilinkBacklinksUseCaseTest {
    @Test
    fun listsLoreAndSessionMentionsOfATarget() = runTest {
        val now = Instant.parse("2026-09-06T12:00:00Z")
        val loreRepository = FakeLoreRepository()
        val campaignRepository = FakeCampaignRepository()
        val sessionRepository = FakeSessionRepository()
        val locationRepository = FakeLocationRepository()
        val factionRepository = FakeFactionRepository()
        val worldPersonRepository = FakeWorldPersonRepository()
        val campaignPersonRepository = FakeCampaignPersonRepository()
        val questRepository = FakeQuestRepository()
        val inn = Location(
            id = "loc-1",
            worldId = "world-1",
            type = LocationType.Place,
            parentLocationId = null,
            name = "Blackbriar Inn",
            description = "",
            climate = "",
            terrain = "",
            government = "",
            landmarks = emptyList(),
            history = "",
            notes = "",
            createdAt = now,
            updatedAt = now,
        )
        locationRepository.insert(inn)
        loreRepository.insert(
            Lore(
                id = "lore-1",
                worldId = "world-1",
                title = "The Inn",
                content = "The party met at [[location:loc-1|Blackbriar Inn]].",
                category = LoreCategory.Other,
                tags = emptyList(),
                relatedEntryIds = emptyList(),
                secrets = emptyList(),
                locationId = null,
                characterId = null,
                createdAt = now,
                updatedAt = now,
            )
        )
        campaignRepository.insert(
            Campaign(
                id = "camp-1",
                worldId = "world-1",
                name = "Run",
                description = "",
                notes = "",
                gameSystem = null,
                status = CampaignStatus.Active,
                createdAt = now,
                updatedAt = now,
            )
        )
        sessionRepository.insert(
            Session(
                id = "sess-1",
                campaignId = "camp-1",
                name = "Session 1",
                notes = "Drinks at [[Blackbriar Inn]].",
                recap = "",
                scratchNotes = "",
                scenes = emptyList(),
                marchOrder = emptyList(),
                createdAt = now,
                updatedAt = now,
            )
        )
        val listBacklinks = ListWikilinkBacklinksUseCase(
            loreRepository = loreRepository,
            campaignRepository = campaignRepository,
            sessionRepository = sessionRepository,
            loadCatalog = LoadWikilinkCatalogUseCase(
                campaignRepository = campaignRepository,
                locationRepository = locationRepository,
                loreRepository = loreRepository,
                factionRepository = factionRepository,
                worldPersonRepository = worldPersonRepository,
                campaignPersonRepository = campaignPersonRepository,
                questRepository = questRepository,
                sessionRepository = sessionRepository,
            ),
        )

        val backlinks = listBacklinks(worldId = "world-1", targetId = "loc-1")

        assertEquals(2, backlinks.size)
        assertEquals(WikilinkSourceKind.Lore, backlinks[0].sourceKind)
        assertEquals("The Inn", backlinks[0].sourceTitle)
        assertEquals(WikilinkSourceKind.SessionNotes, backlinks[1].sourceKind)
        assertEquals("Session 1", backlinks[1].sourceTitle)
    }
}

package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SearchSessionReferencesUseCaseTest {
    @Test
    fun keepsPeopleLocationsAndLoreInTheActiveContext() = runTest {
        val harness = Harness()
        harness.insertGraph()

        val hits = harness.search("harbor")

        assertEquals(
            listOf(SearchKind.Location, SearchKind.Lore, SearchKind.WorldPerson, SearchKind.CampaignPerson),
            hits.map { it.kind },
        )
        assertEquals(listOf("loc-1", "lore-1", "wp-1", "cp-1"), hits.map { it.id })
    }

    @Test
    fun dropsHitsFromOtherWorldsAndCampaigns() = runTest {
        val harness = Harness()
        harness.insertGraph()
        val now = Instant.parse("2026-08-29T12:00:00Z")
        harness.locations.insert(
            Location(
                id = "loc-other",
                worldId = "world-other",
                type = LocationType.Place,
                parentLocationId = null,
                name = "Harbor other",
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
        )

        val hits = harness.search("harbor")

        assertTrue(hits.none { it.id == "loc-other" })
        assertTrue(hits.none { it.kind == SearchKind.World })
    }

    private class Harness {
        val worlds = FakeWorldRepository()
        val campaigns = FakeCampaignRepository()
        val locations = FakeLocationRepository()
        val lore = FakeLoreRepository()
        val observances = FakeWorldCalendarObservanceRepository()
        val celestialBodies = FakeWorldCelestialBodyRepository()
        val factions = FakeFactionRepository()
        val worldPeople = FakeWorldPersonRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        val quests = FakeQuestRepository()
        val sessions = FakeSessionRepository()
        private val search = SearchSessionReferencesUseCase(
            SearchRecordsUseCase(
                worlds,
                campaigns,
                locations,
                lore,
                observances,
                celestialBodies,
                factions,
                worldPeople,
                campaignPeople,
                quests,
                sessions,
            )
        )

        suspend fun search(query: String): List<SearchHit> {
            return search.invoke(query, worldId = "world-1", campaignId = "campaign-1")
        }

        suspend fun insertGraph() {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            worlds.insert(World("world-1", "Faerun", "", GameSystem.FifthEdition, now, now))
            campaigns.insert(
                Campaign(
                    id = "campaign-1",
                    worldId = "world-1",
                    name = "Harbor run",
                    description = "",
                    notes = "",
                    gameSystem = null,
                    status = CampaignStatus.Active,
                    createdAt = now,
                    updatedAt = now,
                )
            )
            locations.insert(
                Location(
                    id = "loc-1",
                    worldId = "world-1",
                    type = LocationType.City,
                    parentLocationId = null,
                    name = "Harbor",
                    description = "Docks",
                    climate = "",
                    terrain = "",
                    government = "",
                    landmarks = emptyList(),
                    history = "",
                    notes = "",
                    createdAt = now,
                    updatedAt = now,
                )
            )
            lore.insert(
                Lore(
                    id = "lore-1",
                    worldId = "world-1",
                    title = "Harbor pact",
                    content = "A pact at the harbor.",
                    category = LoreCategory.History,
                    tags = emptyList(),
                    relatedEntryIds = emptyList(),
                    secrets = emptyList(),
                    locationId = null,
                    characterId = null,
                    createdAt = now,
                    updatedAt = now,
                )
            )
            worldPeople.insert(
                WorldPerson(
                    id = "wp-1",
                    worldId = "world-1",
                    kind = PersonKind.Npc,
                    name = "Harbor master",
                    description = "",
                    sheet = FifthEditionSheet.empty(),
                    createdAt = now,
                    updatedAt = now,
                )
            )
            campaignPeople.insert(
                CampaignPerson(
                    id = "cp-1",
                    campaignId = "campaign-1",
                    worldPersonId = null,
                    kind = PersonKind.Npc,
                    name = "Harbor thief",
                    description = "",
                    sheet = FifthEditionSheet.empty(),
                    overlayHitPoints = null,
                    overlayNotes = "",
                    createdAt = now,
                    updatedAt = now,
                )
            )
        }
    }
}

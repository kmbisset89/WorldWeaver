package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

internal class LoadSessionReferencePeekUseCaseTest {
    @Test
    fun loadsLocationCampaignNotesAndLoreSecrets() = runTest {
        val harness = Harness()
        harness.seed()

        val location = assertIs<SessionReferencePeek.Location>(
            harness.load(
                SearchHit(
                    kind = SearchKind.Location,
                    id = "loc-1",
                    title = "Harbor",
                    snippet = "",
                    worldId = "world-1",
                    campaignId = null,
                )
            )
        )
        assertEquals("Harbor", location.title)
        assertEquals("City", location.typeLabel)
        assertEquals("Docks", location.description)
        assertEquals("Party slept here", location.campaignNotes)

        val lore = assertIs<SessionReferencePeek.Lore>(
            harness.load(
                SearchHit(
                    kind = SearchKind.Lore,
                    id = "lore-1",
                    title = "Harbor pact",
                    snippet = "",
                    worldId = "world-1",
                    campaignId = null,
                )
            )
        )
        assertEquals("History", lore.categoryLabel)
        assertEquals("Hidden terms", lore.secrets.single().title)
        assertEquals("The council still meets", lore.secrets.single().secret)
    }

    @Test
    fun returnsNullForUnknownRecords() = runTest {
        val harness = Harness()

        val missing = harness.load(
            SearchHit(
                kind = SearchKind.Location,
                id = "missing",
                title = "Gone",
                snippet = "",
                worldId = "world-1",
                campaignId = null,
            )
        )

        assertNull(missing)
    }

    private class Harness {
        val locations = FakeLocationRepository()
        val overlays = FakeLocationOverlayRepository()
        val lore = FakeLoreRepository()
        val worldPeople = FakeWorldPersonRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        private val loadPeek = LoadSessionReferencePeekUseCase(
            locations,
            overlays,
            lore,
            worldPeople,
            campaignPeople,
        )

        suspend fun load(hit: SearchHit): SessionReferencePeek? {
            return loadPeek(hit, campaignId = "campaign-1")
        }

        suspend fun seed() {
            val now = Instant.parse("2026-08-29T12:00:00Z")
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
            overlays.upsert(
                LocationOverlay(
                    campaignId = "campaign-1",
                    locationId = "loc-1",
                    hasPartyPresence = true,
                    notes = "Party slept here",
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
                    secrets = listOf(
                        LoreSecret(
                            id = "secret-1",
                            title = "Hidden terms",
                            secret = "The council still meets",
                            hints = emptyList(),
                        )
                    ),
                    locationId = null,
                    characterId = null,
                    createdAt = now,
                    updatedAt = now,
                )
            )
        }
    }
}

package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SearchRecordsUseCaseTest {
    @Test
    fun blankOrShortQueryReturnsNothing() = runTest {
        val harness = Harness()
        harness.insertLore(title = "The Sundering", content = "The world split.")

        assertTrue(harness.search("").isEmpty())
        assertTrue(harness.search(" ").isEmpty())
        assertTrue(harness.search("T").isEmpty())
    }

    @Test
    fun matchesTitleAndDoesNotMatchSecretText() = runTest {
        val harness = Harness()
        harness.insertLore(
            title = "The Sundering",
            content = "The world split.",
            secrets = listOf(
                LoreSecret(
                    id = "secret-1",
                    title = "Hidden",
                    secret = "The gods still walk Ten Towns",
                    hints = listOf(LoreHint(id = "hint-1", text = "Ask the speaker", revealed = false)),
                ),
            ),
        )

        val titleHits = harness.search("Sundering")
        assertEquals(1, titleHits.size)
        assertEquals("The Sundering", titleHits.single().title)
        assertEquals("The world split.", titleHits.single().snippet)
        assertTrue(titleHits.single().snippet.contains("gods").not())

        val secretHits = harness.search("Ten Towns")
        assertTrue(secretHits.isEmpty())
    }

    @Test
    fun matchesWorldName() = runTest {
        val harness = Harness()
        val now = Instant.parse("2026-08-29T12:00:00Z")
        harness.worlds.insert(
            World("w-1", "Faerun", "Sword Coast", GameSystem.FifthEdition, now, now),
        )

        val hits = harness.search("Faerun")
        assertEquals(1, hits.size)
        assertEquals(SearchKind.World, hits.single().kind)
        assertEquals("w-1", hits.single().worldId)
    }

    private class Harness {
        val worlds = FakeWorldRepository()
        val campaigns = FakeCampaignRepository()
        val locations = FakeLocationRepository()
        val lore = FakeLoreRepository()
        val factions = FakeFactionRepository()
        val worldPeople = FakeWorldPersonRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        val quests = FakeQuestRepository()
        val sessions = FakeSessionRepository()
        private val searchRecords = SearchRecordsUseCase(
            worlds,
            campaigns,
            locations,
            lore,
            factions,
            worldPeople,
            campaignPeople,
            quests,
            sessions,
        )

        suspend fun search(query: String): List<SearchHit> {
            return searchRecords(query)
        }

        suspend fun insertLore(
            title: String,
            content: String,
            secrets: List<LoreSecret> = emptyList(),
        ) {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            lore.insert(
                Lore(
                    id = "lore-1",
                    worldId = "world-1",
                    title = title,
                    content = content,
                    category = LoreCategory.History,
                    tags = emptyList(),
                    relatedEntryIds = emptyList(),
                    secrets = secrets,
                    locationId = null,
                    characterId = null,
                    createdAt = now,
                    updatedAt = now,
                )
            )
        }
    }
}

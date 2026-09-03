package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class DeleteLoreUseCaseTest {
    @Test
    fun deleteRemovesEntryAndCleansRelatedLinks() = runTest {
        val harness = Harness()
        val target = harness.insert("lore-1", "Target", related = emptyList())
        harness.insert("lore-2", "Linked", related = listOf(target.id, "already-missing"))

        val result = harness.deleteLore(target.id)

        assertIs<DeleteLoreUseCase.Result.Deleted>(result)
        assertTrue(harness.lore.all().none { it.id == target.id })
        assertEquals(listOf("already-missing"), harness.lore.getById("lore-2")?.relatedEntryIds)
    }

    @Test
    fun deleteReturnsNotFound() = runTest {
        val harness = Harness()

        val result = harness.deleteLore("missing")

        assertIs<DeleteLoreUseCase.Result.NotFound>(result)
    }

    private class Harness {
        val lore = FakeLoreRepository()
        val quests = FakeQuestRepository()
        val deleteLore = DeleteLoreUseCase(lore, quests)

        suspend fun insert(
            id: String,
            title: String,
            related: List<String>,
        ): Lore {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            val entry = Lore(
                id = id,
                worldId = "world-1",
                title = title,
                content = "Body",
                category = LoreCategory.History,
                tags = emptyList(),
                relatedEntryIds = related,
                secrets = emptyList(),
                locationId = null,
                characterId = null,
                createdAt = now,
                updatedAt = now,
            )
            lore.insert(entry)
            return entry
        }
    }
}

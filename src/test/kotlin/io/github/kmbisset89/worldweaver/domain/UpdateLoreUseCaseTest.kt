package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class UpdateLoreUseCaseTest {
    @Test
    fun updatePersistsCategoryTagsAndSecrets() = runTest {
        val harness = Harness()
        val existing = harness.insert()

        val result = harness.updateLore(
            existing.id,
            LoreDraft(
                title = "Updated",
                content = "New body",
                category = LoreCategory.Religion,
                tags = listOf("worship"),
                relatedEntryIds = emptyList(),
                secrets = listOf(
                    LoreSecret(
                        id = "secret-1",
                        title = "Hidden rite",
                        secret = "Midnight only",
                        hints = listOf(
                            LoreHint(id = "hint-1", text = "Listen for bells", revealed = true),
                        ),
                    )
                ),
                locationId = null,
                characterId = null,
            ),
        )

        assertIs<UpdateLoreUseCase.Result.Updated>(result)
        val updated = harness.lore.getById(existing.id)
        assertEquals("Updated", updated?.title)
        assertEquals(LoreCategory.Religion, updated?.category)
        assertEquals(listOf("worship"), updated?.tags)
        assertEquals("Hidden rite", updated?.secrets?.first()?.title)
        assertEquals(true, updated?.secrets?.first()?.hints?.first()?.revealed)
    }

    @Test
    fun updateRejectsEmptyContent() = runTest {
        val harness = Harness()
        val existing = harness.insert()

        val result = harness.updateLore(
            existing.id,
            LoreDraft(
                title = "Updated",
                content = "  ",
                category = LoreCategory.Other,
                tags = emptyList(),
                relatedEntryIds = emptyList(),
                secrets = emptyList(),
                locationId = null,
                characterId = null,
            ),
        )

        assertIs<UpdateLoreUseCase.Result.InvalidContent>(result)
        assertEquals("Original", harness.lore.getById(existing.id)?.title)
    }

    private class Harness {
        val lore = FakeLoreRepository()
        val locations = FakeLocationRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T13:00:00Z") }
        private val ids = EntityIdFactory { "generated" }
        val updateLore = UpdateLoreUseCase(lore, locations, ids, instant)

        suspend fun insert(): Lore {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            val entry = Lore(
                id = "lore-1",
                worldId = "world-1",
                title = "Original",
                content = "Body",
                category = LoreCategory.History,
                tags = emptyList(),
                relatedEntryIds = emptyList(),
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

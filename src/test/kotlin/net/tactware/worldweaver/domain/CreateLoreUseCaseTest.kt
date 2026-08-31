package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class CreateLoreUseCaseTest {
    @Test
    fun createRequiresActiveWorld() = runTest {
        val harness = Harness()

        val result = harness.createLore(harness.draft())

        assertIs<CreateLoreUseCase.Result.NoActiveWorld>(result)
        assertTrue(harness.lore.all().isEmpty())
    }

    @Test
    fun createRejectsBlankTitle() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")

        val result = harness.createLore(harness.draft(title = "  "))

        assertIs<CreateLoreUseCase.Result.InvalidTitle>(result)
    }

    @Test
    fun createRejectsBlankContent() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")

        val result = harness.createLore(harness.draft(content = " "))

        assertIs<CreateLoreUseCase.Result.InvalidContent>(result)
    }

    @Test
    fun createStoresWorldOwnedEntry() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")

        val result = harness.createLore(
            harness.draft(
                title = "The Sundering",
                content = "The world split.",
                category = LoreCategory.History,
                tags = listOf("gods", "  war", ""),
            )
        )

        val created = assertIs<CreateLoreUseCase.Result.Created>(result)
        assertEquals("world-1", created.lore.worldId)
        assertEquals("The Sundering", created.lore.title)
        assertEquals(listOf("gods", "war"), created.lore.tags)
        assertEquals(LoreCategory.History, created.lore.category)
        assertNull(created.lore.locationId)
    }

    @Test
    fun createAttachesValidLocationAndDropsBrokenRelatedIds() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")
        val location = harness.insertLocation("Waterdeep")
        val related = harness.insertLore("related-1", "Old Myth")

        val result = harness.createLore(
            harness.draft(
                title = "New Myth",
                content = "A tale",
                relatedEntryIds = listOf(related.id, "missing"),
                locationId = location.id,
            )
        )

        val created = assertIs<CreateLoreUseCase.Result.Created>(result)
        assertEquals(listOf(related.id), created.lore.relatedEntryIds)
        assertEquals(location.id, created.lore.locationId)
    }

    @Test
    fun createRejectsLocationFromAnotherWorld() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")
        val other = harness.insertLocation("Baldur's Gate", worldId = "world-2")

        val result = harness.createLore(harness.draft(locationId = other.id))

        assertIs<CreateLoreUseCase.Result.InvalidLocation>(result)
    }

    @Test
    fun createPersistsDmSecrets() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")

        val result = harness.createLore(
            harness.draft(
                secrets = listOf(
                    LoreSecret(
                        id = "",
                        title = "True name",
                        secret = "The king is a dragon",
                        hints = listOf(
                            LoreHint(id = "", text = "He never ages", revealed = false),
                        ),
                    )
                )
            )
        )

        val created = assertIs<CreateLoreUseCase.Result.Created>(result)
        assertEquals(1, created.lore.secrets.size)
        assertEquals("True name", created.lore.secrets[0].title)
        assertEquals("He never ages", created.lore.secrets[0].hints[0].text)
        assertTrue(created.lore.secrets[0].id.isNotBlank())
    }

    private class Harness {
        val lore = FakeLoreRepository()
        val locations = FakeLocationRepository()
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        private var nextId = 0
        private val ids = EntityIdFactory { "lore-${++nextId}" }
        val createLore = CreateLoreUseCase(lore, locations, context, ids, instant)

        fun draft(
            title: String = "Title",
            content: String = "Content",
            category: LoreCategory = LoreCategory.Other,
            tags: List<String> = emptyList(),
            relatedEntryIds: List<String> = emptyList(),
            secrets: List<LoreSecret> = emptyList(),
            locationId: String? = null,
            characterId: String? = null,
        ): LoreDraft {
            return LoreDraft(
                title = title,
                content = content,
                category = category,
                tags = tags,
                relatedEntryIds = relatedEntryIds,
                secrets = secrets,
                locationId = locationId,
                characterId = characterId,
            )
        }

        suspend fun insertLocation(
            name: String,
            worldId: String = "world-1",
        ): Location {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            val location = Location(
                id = "loc-${name.lowercase()}",
                worldId = worldId,
                type = LocationType.City,
                parentLocationId = null,
                name = name,
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
            locations.insert(location)
            return location
        }

        suspend fun insertLore(id: String, title: String): Lore {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            val entry = Lore(
                id = id,
                worldId = "world-1",
                title = title,
                content = "Body",
                category = LoreCategory.Myth,
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

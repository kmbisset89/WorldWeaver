package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class CreateWorldPersonFromSrdMonsterUseCaseTest {
    @Test
    fun createRequiresActiveWorld() = runTest {
        val harness = Harness()
        harness.seedGoblin()

        val result = harness.createFromMonster("Goblin")

        assertIs<CreateWorldPersonFromSrdMonsterUseCase.Result.NoActiveWorld>(result)
    }

    @Test
    fun unknownMonsterIsNotFound() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")
        harness.seedGoblin()

        val result = harness.createFromMonster("Beholder")

        assertIs<CreateWorldPersonFromSrdMonsterUseCase.Result.NotFound>(result)
        assertEquals(0, harness.people.all().size)
    }

    @Test
    fun createWritesWorldLibraryMonsterSheet() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")
        harness.seedGoblin()

        val result = harness.createFromMonster(" goblin ")

        val created = assertIs<CreateWorldPersonFromSrdMonsterUseCase.Result.Created>(result)
        assertEquals("world-1", created.person.worldId)
        assertEquals(PersonKind.Monster, created.person.kind)
        assertEquals("Goblin", created.person.name)
        assertEquals("humanoid · CR 1/4", created.person.description)
        val sheet = assertIs<FifthEditionSheet>(created.person.sheet)
        assertEquals(7, sheet.hitPoints)
        assertEquals(7, sheet.maxHitPoints)
        assertEquals(15, sheet.armorClass)
        assertEquals(30, sheet.walkSpeed)
        assertEquals("CR 1/4. humanoid.", sheet.notes)
    }

    private class Harness {
        val people = FakeWorldPersonRepository()
        val catalogs = FakeSrdCatalogRepository()
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-31T12:00:00Z") }
        private var nextId = 0
        private val ids = EntityIdFactory { "person-${++nextId}" }
        val createFromMonster = CreateWorldPersonFromSrdMonsterUseCase(
            createWorldPerson = CreateWorldPersonUseCase(people, context, ids, instant),
            catalogRepository = catalogs,
            resolver = FifthEditionPickerCatalogResolver(),
        )

        suspend fun seedGoblin() {
            catalogs.write(
                SrdCatalog(
                    formatVersion = 1,
                    sourceLabel = "5E SRD 5.1",
                    importedAt = Instant.parse("2026-08-31T12:00:00Z"),
                    races = emptyList(),
                    classes = emptyList(),
                    spells = emptyList(),
                    monsters = listOf(
                        SrdMonsterEntry("Goblin", "humanoid", "1/4", 7, 15, 30),
                    ),
                ),
            )
        }
    }
}

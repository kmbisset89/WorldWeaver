package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class CreateWorldPersonUseCaseTest {
    @Test
    fun createRequiresActiveWorld() = runTest {
        val harness = Harness()

        val result = harness.createPerson(name = "Bram")

        assertIs<CreateWorldPersonUseCase.Result.NoActiveWorld>(result)
        assertTrue(harness.worldPeople.all().isEmpty())
    }

    @Test
    fun createRejectsBlankName() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")

        val result = harness.createPerson(name = "  ")

        assertIs<CreateWorldPersonUseCase.Result.InvalidName>(result)
    }

    @Test
    fun createRejectsPlayerCharacterKind() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")

        val result = harness.createPerson(name = "Aelar", kind = PersonKind.PlayerCharacter)

        assertIs<CreateWorldPersonUseCase.Result.InvalidKind>(result)
        assertTrue(harness.worldPeople.all().isEmpty())
    }

    @Test
    fun createPersistsNpcOnActiveWorld() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")

        val result = harness.createPerson(name = "Bram", description = "Innkeeper")

        val created = assertIs<CreateWorldPersonUseCase.Result.Created>(result)
        assertEquals("world-1", created.person.worldId)
        assertEquals(PersonKind.Npc, created.person.kind)
        assertEquals("Bram", created.person.name)
        assertEquals("Innkeeper", created.person.description)
        assertEquals(1, harness.worldPeople.all().size)
    }

    @Test
    fun createPersistsPathfinderSheet() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")
        val sheet = Pathfinder2ESheet.empty().copy(
            ancestry = "Goblin",
            className = "Rogue",
        )

        val result = harness.createWorldPerson(
            WorldPersonDraft(
                kind = PersonKind.Npc,
                name = "Rixi",
                description = "",
                sheet = sheet,
            )
        )

        val created = assertIs<CreateWorldPersonUseCase.Result.Created>(result)
        val persisted = assertIs<Pathfinder2ESheet>(created.person.sheet)
        assertEquals("Goblin", persisted.ancestry)
        assertEquals("Rogue", persisted.className)
        assertEquals(GameSystem.Pathfinder2E, persisted.gameSystem())
    }

    private class Harness {
        val worldPeople = FakeWorldPersonRepository()
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        private var nextId = 0
        private val ids = EntityIdFactory { "world-person-${++nextId}" }
        val createWorldPerson = CreateWorldPersonUseCase(worldPeople, context, ids, instant)

        suspend fun createPerson(
            name: String,
            description: String = "",
            kind: PersonKind = PersonKind.Npc,
        ): CreateWorldPersonUseCase.Result {
            return createWorldPerson(
                WorldPersonDraft(
                    kind = kind,
                    name = name,
                    description = description,
                    sheet = FifthEditionSheet.empty(),
                )
            )
        }
    }
}

package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class CreateFactionUseCaseTest {
    @Test
    fun createRequiresActiveWorld() = runTest {
        val harness = Harness()

        val result = harness.createFaction(FactionDraft("Harpers", "", "", ""))

        assertIs<CreateFactionUseCase.Result.NoActiveWorld>(result)
        assertTrue(harness.factions.all().isEmpty())
    }

    @Test
    fun createRejectsBlankName() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")

        val result = harness.createFaction(FactionDraft("  ", "desc", "", ""))

        assertIs<CreateFactionUseCase.Result.InvalidName>(result)
    }

    @Test
    fun createRejectsDuplicateName() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")
        harness.createFaction(FactionDraft("Harpers", "", "", ""))

        val result = harness.createFaction(FactionDraft("harpers", "", "", ""))

        assertIs<CreateFactionUseCase.Result.DuplicateName>(result)
        assertEquals(1, harness.factions.all().size)
    }

    @Test
    fun createStoresWorldOwnedFaction() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")

        val result = harness.createFaction(
            FactionDraft(
                name = "  Harpers  ",
                description = " Spies ",
                goals = " Balance ",
                notes = " DM only ",
            )
        )

        val created = assertIs<CreateFactionUseCase.Result.Created>(result)
        assertEquals("world-1", created.faction.worldId)
        assertEquals("Harpers", created.faction.name)
        assertEquals("Spies", created.faction.description)
        assertEquals("Balance", created.faction.goals)
        assertEquals("DM only", created.faction.notes)
    }

    private class Harness {
        val factions = FakeFactionRepository()
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        private var nextId = 0
        private val ids = EntityIdFactory { "fac-${++nextId}" }
        val createFaction = CreateFactionUseCase(factions, context, ids, instant)
    }
}

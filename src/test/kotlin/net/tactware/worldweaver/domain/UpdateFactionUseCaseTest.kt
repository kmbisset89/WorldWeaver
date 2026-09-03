package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class UpdateFactionUseCaseTest {
    @Test
    fun updateRenamesFaction() = runTest {
        val harness = Harness()
        val faction = harness.insertFaction("Harpers")

        val result = harness.updateFaction(
            faction.id,
            FactionDraft("Zhentarim", "Merchants", "Power", "Notes"),
        )

        assertIs<UpdateFactionUseCase.Result.Updated>(result)
        assertEquals("Zhentarim", harness.factions.getById(faction.id)?.name)
        assertEquals("Merchants", harness.factions.getById(faction.id)?.description)
    }

    @Test
    fun updateRejectsDuplicateName() = runTest {
        val harness = Harness()
        harness.insertFaction("Harpers")
        val other = harness.insertFaction("Zhentarim")

        val result = harness.updateFaction(other.id, FactionDraft("harpers", "", "", ""))

        assertIs<UpdateFactionUseCase.Result.DuplicateName>(result)
        assertEquals("Zhentarim", harness.factions.getById(other.id)?.name)
    }

    private class Harness {
        val factions = FakeFactionRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        private var nextId = 0
        val updateFaction = UpdateFactionUseCase(factions, instant)

        suspend fun insertFaction(name: String): Faction {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            val faction = Faction(
                id = "fac-${++nextId}",
                worldId = "world-1",
                name = name,
                description = "",
                goals = "",
                notes = "",
                createdAt = now,
                updatedAt = now,
            )
            factions.insert(faction)
            return faction
        }
    }
}

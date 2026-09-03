package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

internal class DeleteFactionUseCaseTest {
    @Test
    fun deleteRemovesUnreferencedFaction() = runTest {
        val harness = Harness()
        val faction = harness.insertFaction("Harpers")

        val result = harness.deleteFaction(faction.id)

        assertIs<DeleteFactionUseCase.Result.Deleted>(result)
        assertNull(harness.factions.getById(faction.id))
    }

    @Test
    fun deleteIsBlockedWhenMembershipExists() = runTest {
        val harness = Harness()
        val faction = harness.insertFaction("Harpers")
        harness.memberships.insert(
            FactionMembership(
                id = "mem-1",
                person = PersonRef.World("person-1"),
                factionId = faction.id,
                role = "Agent",
                notes = "",
                createdAt = Instant.parse("2026-08-29T12:00:00Z"),
            )
        )

        val result = harness.deleteFaction(faction.id)

        val blocked = assertIs<DeleteFactionUseCase.Result.Blocked>(result)
        assertEquals(1, blocked.membershipCount)
        assertEquals(0, blocked.relationshipCount)
        assertEquals(faction.id, harness.factions.getById(faction.id)?.id)
    }

    @Test
    fun deleteIsBlockedWhenRelationshipLeansOnFaction() = runTest {
        val harness = Harness()
        val faction = harness.insertFaction("Harpers")
        harness.relationships.insert(
            PersonRelationship(
                id = "rel-1",
                from = PersonRef.World("a"),
                to = PersonRef.World("b"),
                type = RelationshipType.Ally,
                description = "",
                factionId = faction.id,
            )
        )

        val result = harness.deleteFaction(faction.id)

        val blocked = assertIs<DeleteFactionUseCase.Result.Blocked>(result)
        assertEquals(0, blocked.membershipCount)
        assertEquals(1, blocked.relationshipCount)
    }

    private class Harness {
        val factions = FakeFactionRepository()
        val memberships = FakeFactionMembershipRepository()
        val relationships = FakePersonRelationshipRepository()
        val deleteFaction = DeleteFactionUseCase(factions, memberships, relationships)

        suspend fun insertFaction(name: String): Faction {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            val faction = Faction(
                id = "fac-$name",
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

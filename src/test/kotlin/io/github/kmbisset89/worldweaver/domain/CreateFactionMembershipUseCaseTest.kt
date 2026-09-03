package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class CreateFactionMembershipUseCaseTest {
    @Test
    fun createAddsWorldPersonToFaction() = runTest {
        val harness = Harness()
        val person = harness.insertWorldPerson("Bram")
        val faction = harness.insertFaction("Harpers")

        val result = harness.createMembership(
            person = PersonRef.World(person.id),
            factionId = faction.id,
            role = " Agent ",
        )

        val created = assertIs<CreateFactionMembershipUseCase.Result.Created>(result)
        assertEquals(faction.id, created.membership.factionId)
        assertEquals("Agent", created.membership.role)
        assertEquals(1, harness.memberships.all().size)
    }

    @Test
    fun createRejectsWrongWorld() = runTest {
        val harness = Harness()
        val person = harness.insertWorldPerson("Bram")
        val faction = harness.insertFaction("Harpers", worldId = "world-2")

        val result = harness.createMembership(
            person = PersonRef.World(person.id),
            factionId = faction.id,
        )

        assertIs<CreateFactionMembershipUseCase.Result.WrongWorld>(result)
        assertTrue(harness.memberships.all().isEmpty())
    }

    @Test
    fun createRejectsDuplicate() = runTest {
        val harness = Harness()
        val person = harness.insertWorldPerson("Bram")
        val faction = harness.insertFaction("Harpers")
        harness.createMembership(PersonRef.World(person.id), faction.id)

        val result = harness.createMembership(PersonRef.World(person.id), faction.id)

        assertIs<CreateFactionMembershipUseCase.Result.Duplicate>(result)
        assertEquals(1, harness.memberships.all().size)
    }

    private class Harness {
        val memberships = FakeFactionMembershipRepository()
        val factions = FakeFactionRepository()
        val worldPeople = FakeWorldPersonRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        val campaigns = FakeCampaignRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        private var nextId = 0
        private val ids = EntityIdFactory { "mem-${++nextId}" }
        val createMembership = CreateFactionMembershipUseCase(
            memberships,
            factions,
            worldPeople,
            campaignPeople,
            campaigns,
            ids,
            instant,
        )
        private val now = Instant.parse("2026-08-29T12:00:00Z")

        suspend fun insertWorldPerson(name: String): WorldPerson {
            val person = WorldPerson(
                id = "world-person-${++nextId}",
                worldId = "world-1",
                kind = PersonKind.Npc,
                name = name,
                description = "",
                sheet = FifthEditionSheet.empty(),
                createdAt = now,
                updatedAt = now,
            )
            worldPeople.insert(person)
            return person
        }

        suspend fun insertFaction(name: String, worldId: String = "world-1"): Faction {
            val faction = Faction(
                id = "fac-${++nextId}",
                worldId = worldId,
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

        suspend fun createMembership(
            person: PersonRef,
            factionId: String,
            role: String = "",
        ): CreateFactionMembershipUseCase.Result {
            return createMembership(person, factionId, role, "")
        }
    }
}

package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class CreatePersonRelationshipUseCaseTest {
    @Test
    fun relationshipCanTargetWorldAndCampaignPeople() = runTest {
        val harness = Harness()
        val worldPerson = harness.insertWorldPerson("Bram")
        val campaignPerson = harness.insertCampaignPerson("Aelar")
        val faction = harness.insertFaction("Harpers")

        val result = harness.createRelationship(
            from = PersonRef.World(worldPerson.id),
            to = PersonRef.Campaign(campaignPerson.id),
            type = RelationshipType.Ally,
            factionId = faction.id,
        )

        val created = assertIs<CreatePersonRelationshipUseCase.Result.Created>(result)
        assertEquals(worldPerson.id, created.relationship.from.id)
        assertIs<PersonRef.World>(created.relationship.from)
        assertEquals(campaignPerson.id, created.relationship.to.id)
        assertIs<PersonRef.Campaign>(created.relationship.to)
        assertEquals(faction.id, created.relationship.factionId)
        assertEquals(1, harness.relationships.all().size)
    }

    @Test
    fun selfRelationshipIsRejected() = runTest {
        val harness = Harness()
        val person = harness.insertWorldPerson("Bram")

        val result = harness.createRelationship(
            from = PersonRef.World(person.id),
            to = PersonRef.World(person.id),
            type = RelationshipType.Rival,
        )

        assertIs<CreatePersonRelationshipUseCase.Result.SelfRelationship>(result)
        assertTrue(harness.relationships.all().isEmpty())
    }

    @Test
    fun missingTargetIsRejected() = runTest {
        val harness = Harness()
        val person = harness.insertWorldPerson("Bram")

        val result = harness.createRelationship(
            from = PersonRef.World(person.id),
            to = PersonRef.Campaign("missing"),
            type = RelationshipType.Enemy,
        )

        assertIs<CreatePersonRelationshipUseCase.Result.InvalidTarget>(result)
    }

    @Test
    fun factionFromAnotherWorldIsRejected() = runTest {
        val harness = Harness()
        val from = harness.insertWorldPerson("Bram")
        val to = harness.insertWorldPerson("Cora")
        val faction = harness.insertFaction("Harpers", worldId = "world-2")

        val result = harness.createRelationship(
            from = PersonRef.World(from.id),
            to = PersonRef.World(to.id),
            type = RelationshipType.Ally,
            factionId = faction.id,
        )

        assertIs<CreatePersonRelationshipUseCase.Result.WrongWorld>(result)
    }

    private class Harness {
        val worldPeople = FakeWorldPersonRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        val campaigns = FakeCampaignRepository()
        val factions = FakeFactionRepository()
        val relationships = FakePersonRelationshipRepository()
        private var nextId = 0
        private val ids = EntityIdFactory { "rel-${++nextId}" }
        val createPersonRelationship = CreatePersonRelationshipUseCase(
            relationships,
            worldPeople,
            campaignPeople,
            campaigns,
            factions,
            ids,
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

        suspend fun insertCampaignPerson(name: String): CampaignPerson {
            campaigns.insert(
                Campaign(
                    id = "campaign-1",
                    worldId = "world-1",
                    name = "Main",
                    description = "",
                    notes = "",
                    gameSystem = GameSystem.FifthEdition,
                    status = CampaignStatus.Active,
                    createdAt = now,
                    updatedAt = now,
                )
            )
            val person = CampaignPerson(
                id = "campaign-person-${++nextId}",
                campaignId = "campaign-1",
                worldPersonId = null,
                kind = PersonKind.PlayerCharacter,
                name = name,
                description = "",
                sheet = FifthEditionSheet.empty(),
                overlayHitPoints = null,
                overlayNotes = "",
                createdAt = now,
                updatedAt = now,
            )
            campaignPeople.insert(person)
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

        suspend fun createRelationship(
            from: PersonRef,
            to: PersonRef,
            type: RelationshipType,
            factionId: String? = null,
        ): CreatePersonRelationshipUseCase.Result {
            return createPersonRelationship(
                from = from,
                to = to,
                type = type,
                description = "",
                factionId = factionId,
            )
        }
    }
}

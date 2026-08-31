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

        val result = harness.createRelationship(
            from = PersonRef.World(worldPerson.id),
            to = PersonRef.Campaign(campaignPerson.id),
            type = RelationshipType.Ally,
            factionLean = "Harpers",
        )

        val created = assertIs<CreatePersonRelationshipUseCase.Result.Created>(result)
        assertEquals(worldPerson.id, created.relationship.from.id)
        assertIs<PersonRef.World>(created.relationship.from)
        assertEquals(campaignPerson.id, created.relationship.to.id)
        assertIs<PersonRef.Campaign>(created.relationship.to)
        assertEquals("Harpers", created.relationship.factionLean)
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

    private class Harness {
        val worldPeople = FakeWorldPersonRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        val relationships = FakePersonRelationshipRepository()
        private var nextId = 0
        private val ids = EntityIdFactory { "rel-${++nextId}" }
        val createPersonRelationship = CreatePersonRelationshipUseCase(
            relationships,
            worldPeople,
            campaignPeople,
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

        suspend fun createRelationship(
            from: PersonRef,
            to: PersonRef,
            type: RelationshipType,
            factionLean: String = "",
        ): CreatePersonRelationshipUseCase.Result {
            return createPersonRelationship(
                from = from,
                to = to,
                type = type,
                description = "",
                factionLean = factionLean,
            )
        }
    }
}

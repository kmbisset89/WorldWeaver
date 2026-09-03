package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class DeleteWorldPersonUseCaseTest {
    @Test
    fun deleteIsBlockedWhenCampaignReferencesExist() = runTest {
        val harness = Harness()
        val person = harness.insertWorldPerson("Bram")
        harness.insertCampaignReference(person.id)

        val result = harness.deletePerson(person.id)

        val blocked = assertIs<DeleteWorldPersonUseCase.Result.Blocked>(result)
        assertEquals(1, blocked.referenceCount)
        assertEquals(person.id, harness.worldPeople.getById(person.id)?.id)
        assertEquals(1, harness.campaignPeople.all().size)
    }

    @Test
    fun deleteRemovesUnreferencedPersonAndRelationships() = runTest {
        val harness = Harness()
        val person = harness.insertWorldPerson("Bram")
        val other = harness.insertWorldPerson("Cora")
        harness.relationships.insert(
            PersonRelationship(
                id = "rel-1",
                from = PersonRef.World(person.id),
                to = PersonRef.World(other.id),
                type = RelationshipType.Ally,
                description = "",
                factionId = null,
            )
        )
        harness.companions.insert(
            PersonCompanion(
                id = "companion-1",
                owner = PersonRef.World(person.id),
                companion = PersonRef.World(other.id),
                kind = CompanionKind.Familiar,
            )
        )

        val result = harness.deletePerson(person.id)

        assertIs<DeleteWorldPersonUseCase.Result.Deleted>(result)
        assertTrue(harness.worldPeople.all().none { it.id == person.id })
        assertTrue(harness.relationships.all().isEmpty())
        assertTrue(harness.companions.all().isEmpty())
        assertEquals(other.id, harness.worldPeople.getById(other.id)?.id)
    }

    private class Harness {
        val worldPeople = FakeWorldPersonRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        val relationships = FakePersonRelationshipRepository()
        val memberships = FakeFactionMembershipRepository()
        val companions = FakePersonCompanionRepository()
        val quests = FakeQuestRepository()
        val avatars = PersonAvatarFileStore(Files.createTempDirectory("ww-avatars").toFile())
        val voices = VoiceClipFileStore(Files.createTempDirectory("ww-voices").toFile())
        val deletePerson = DeleteWorldPersonUseCase(
            worldPeople,
            campaignPeople,
            relationships,
            memberships,
            companions,
            quests,
            avatars,
            voices,
        )
        private val now = Instant.parse("2026-08-29T12:00:00Z")
        private var nextId = 0

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

        suspend fun insertCampaignReference(worldPersonId: String) {
            campaignPeople.insert(
                CampaignPerson(
                    id = "campaign-person-1",
                    campaignId = "campaign-1",
                    worldPersonId = worldPersonId,
                    kind = PersonKind.Npc,
                    name = "Bram",
                    description = "",
                    sheet = FifthEditionSheet.empty(),
                    overlayHitPoints = 10,
                    overlayNotes = "",
                    createdAt = now,
                    updatedAt = now,
                )
            )
        }
    }
}

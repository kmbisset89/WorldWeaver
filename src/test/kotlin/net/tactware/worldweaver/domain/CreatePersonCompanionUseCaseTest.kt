package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class CreatePersonCompanionUseCaseTest {
    @Test
    fun missingTargetIsRejected() = runTest {
        val harness = Harness()
        val owner = harness.insertCampaignPerson("Aelar", PersonKind.PlayerCharacter)

        val result = harness.createCompanion(
            owner = PersonRef.Campaign(owner.id),
            companion = PersonRef.Campaign("missing"),
            kind = CompanionKind.Familiar,
        )

        assertIs<CreatePersonCompanionUseCase.Result.InvalidTarget>(result)
        assertTrue(harness.companions.all().isEmpty())
    }

    @Test
    fun selfCompanionIsRejected() = runTest {
        val harness = Harness()
        val owner = harness.insertWorldPerson("Owl", PersonKind.Monster)

        val result = harness.createCompanion(
            owner = PersonRef.World(owner.id),
            companion = PersonRef.World(owner.id),
            kind = CompanionKind.Familiar,
        )

        assertIs<CreatePersonCompanionUseCase.Result.SelfCompanion>(result)
        assertTrue(harness.companions.all().isEmpty())
    }

    @Test
    fun playerCharacterCompanionIsRejected() = runTest {
        val harness = Harness()
        val owner = harness.insertCampaignPerson("Aelar", PersonKind.PlayerCharacter)
        val other = harness.insertCampaignPerson("Mira", PersonKind.PlayerCharacter)

        val result = harness.createCompanion(
            owner = PersonRef.Campaign(owner.id),
            companion = PersonRef.Campaign(other.id),
            kind = CompanionKind.AnimalCompanion,
        )

        assertIs<CreatePersonCompanionUseCase.Result.InvalidCompanionKind>(result)
        assertTrue(harness.companions.all().isEmpty())
    }

    @Test
    fun duplicateLinkIsRejected() = runTest {
        val harness = Harness()
        val owner = harness.insertCampaignPerson("Aelar", PersonKind.PlayerCharacter)
        val familiar = harness.insertCampaignPerson("Whisper", PersonKind.Monster)
        harness.createCompanion(
            owner = PersonRef.Campaign(owner.id),
            companion = PersonRef.Campaign(familiar.id),
            kind = CompanionKind.Familiar,
        )

        val result = harness.createCompanion(
            owner = PersonRef.Campaign(owner.id),
            companion = PersonRef.Campaign(familiar.id),
            kind = CompanionKind.Familiar,
        )

        assertIs<CreatePersonCompanionUseCase.Result.AlreadyLinked>(result)
        assertEquals(1, harness.companions.all().size)
    }

    @Test
    fun companionLinkPersists() = runTest {
        val harness = Harness()
        val owner = harness.insertWorldPerson("Bram", PersonKind.Npc)
        val familiar = harness.insertWorldPerson("Raven", PersonKind.Monster)

        val result = harness.createCompanion(
            owner = PersonRef.World(owner.id),
            companion = PersonRef.World(familiar.id),
            kind = CompanionKind.Familiar,
        )

        val created = assertIs<CreatePersonCompanionUseCase.Result.Created>(result)
        assertEquals(owner.id, created.companion.owner.id)
        assertIs<PersonRef.World>(created.companion.owner)
        assertEquals(familiar.id, created.companion.companion.id)
        assertEquals(CompanionKind.Familiar, created.companion.kind)
        assertEquals(1, harness.companions.all().size)
    }

    private class Harness {
        val worldPeople = FakeWorldPersonRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        val companions = FakePersonCompanionRepository()
        private var nextId = 0
        private val ids = EntityIdFactory { "companion-${++nextId}" }
        val createPersonCompanion = CreatePersonCompanionUseCase(
            companions,
            worldPeople,
            campaignPeople,
            ids,
        )
        private val now = Instant.parse("2026-08-29T12:00:00Z")

        suspend fun insertWorldPerson(name: String, kind: PersonKind): WorldPerson {
            val person = WorldPerson(
                id = "world-person-${++nextId}",
                worldId = "world-1",
                kind = kind,
                name = name,
                description = "",
                sheet = FifthEditionSheet.empty(),
                createdAt = now,
                updatedAt = now,
            )
            worldPeople.insert(person)
            return person
        }

        suspend fun insertCampaignPerson(name: String, kind: PersonKind): CampaignPerson {
            val person = CampaignPerson(
                id = "campaign-person-${++nextId}",
                campaignId = "campaign-1",
                worldPersonId = null,
                kind = kind,
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

        suspend fun createCompanion(
            owner: PersonRef,
            companion: PersonRef,
            kind: CompanionKind,
        ): CreatePersonCompanionUseCase.Result {
            return createPersonCompanion(
                owner = owner,
                companion = companion,
                kind = kind,
            )
        }
    }
}

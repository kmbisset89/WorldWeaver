package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class AddWorldPersonToCampaignUseCaseTest {
    @Test
    fun addCreatesCampaignReferenceWithOverlayHitPoints() = runTest {
        val harness = Harness()
        harness.context.setActiveCampaignId("campaign-1")
        val worldPerson = harness.insertWorldPerson(
            name = "Bram",
            sheet = FifthEditionSheet.empty().copy(hitPoints = 18, maxHitPoints = 18),
        )

        val result = harness.addToCampaign(worldPerson.id)

        val added = assertIs<AddWorldPersonToCampaignUseCase.Result.Added>(result)
        assertEquals(worldPerson.id, added.person.worldPersonId)
        assertEquals("campaign-1", added.person.campaignId)
        assertEquals("Bram", added.person.name)
        assertEquals(18, added.person.overlayHitPoints)
        assertTrue(added.person.isWorldReference())
        assertEquals(1, harness.worldPeople.all().size)
    }

    @Test
    fun addRejectsDuplicateReferenceInSameCampaign() = runTest {
        val harness = Harness()
        harness.context.setActiveCampaignId("campaign-1")
        val worldPerson = harness.insertWorldPerson(name = "Bram")
        harness.addToCampaign(worldPerson.id)

        val result = harness.addToCampaign(worldPerson.id)

        assertIs<AddWorldPersonToCampaignUseCase.Result.AlreadyAdded>(result)
        assertEquals(1, harness.campaignPeople.all().size)
    }

    @Test
    fun addCopiesWorldVoiceClip() = runTest {
        val harness = Harness()
        harness.context.setActiveCampaignId("campaign-1")
        val worldPerson = harness.insertWorldPerson(name = "Bram")
        val wav = VoiceClipWavFormat.wrapPcm(ByteArray(32))
        harness.voices.write(VoiceClipRef.WorldPerson(worldPerson.id), wav)

        val result = harness.addToCampaign(worldPerson.id)

        val added = assertIs<AddWorldPersonToCampaignUseCase.Result.Added>(result)
        assertNotNull(harness.voices.pathIfPresent(VoiceClipRef.CampaignPerson(added.person.id)))
    }

    private class Harness {
        val worldPeople = FakeWorldPersonRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        private var nextId = 0
        private val ids = EntityIdFactory { "campaign-person-${++nextId}" }
        val voices = VoiceClipFileStore(Files.createTempDirectory("ww-voices").toFile())
        val addToCampaign = AddWorldPersonToCampaignUseCase(
            worldPeople,
            campaignPeople,
            context,
            ids,
            instant,
            PersonAvatarFileStore(Files.createTempDirectory("ww-avatars").toFile()),
            voices,
        )
        private val now = Instant.parse("2026-08-29T12:00:00Z")

        suspend fun insertWorldPerson(
            name: String,
            sheet: FifthEditionSheet = FifthEditionSheet.empty(),
        ): WorldPerson {
            val person = WorldPerson(
                id = "world-person-${++nextId}",
                worldId = "world-1",
                kind = PersonKind.Npc,
                name = name,
                description = "",
                sheet = sheet,
                createdAt = now,
                updatedAt = now,
            )
            worldPeople.insert(person)
            return person
        }
    }
}

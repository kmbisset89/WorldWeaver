package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class SetVoiceClipUseCaseTest {
    @Test
    fun savesValidWavOnWorldPerson() = runTest {
        val harness = Harness()
        val person = harness.insertWorldPerson()
        val wav = VoiceClipWavFormat.wrapPcm(ByteArray(80))

        val result = harness.setVoiceClip(VoiceClipRef.WorldPerson(person.id), wav)

        assertIs<SetVoiceClipUseCase.Result.Saved>(result)
        assertContentEquals(wav, harness.voiceStore.read(VoiceClipRef.WorldPerson(person.id))!!)
        assertEquals(harness.later, harness.worldPeople.getById(person.id)?.updatedAt)
    }

    @Test
    fun savesValidWavOnLocation() = runTest {
        val harness = Harness()
        val location = harness.insertLocation()
        val wav = VoiceClipWavFormat.wrapPcm(ByteArray(60))

        val result = harness.setVoiceClip(VoiceClipRef.Location(location.id), wav)

        assertIs<SetVoiceClipUseCase.Result.Saved>(result)
        assertNotNull(harness.voiceStore.pathIfPresent(VoiceClipRef.Location(location.id)))
    }

    @Test
    fun rejectsInvalidAudio() = runTest {
        val harness = Harness()
        val person = harness.insertWorldPerson()

        val result = harness.setVoiceClip(VoiceClipRef.WorldPerson(person.id), byteArrayOf(1, 2, 3))

        assertIs<SetVoiceClipUseCase.Result.InvalidAudio>(result)
        assertNull(harness.voiceStore.read(VoiceClipRef.WorldPerson(person.id)))
    }

    @Test
    fun rejectsMissingTarget() = runTest {
        val harness = Harness()
        val wav = VoiceClipWavFormat.wrapPcm(ByteArray(40))

        val result = harness.setVoiceClip(VoiceClipRef.CampaignPerson("missing"), wav)

        assertIs<SetVoiceClipUseCase.Result.NotFound>(result)
    }

    private class Harness {
        val worldPeople = FakeWorldPersonRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        val locations = FakeLocationRepository()
        val voiceStore = VoiceClipFileStore(Files.createTempDirectory("ww-voices").toFile())
        val later = Instant.parse("2026-08-31T12:00:00Z")
        private val now = Instant.parse("2026-08-29T12:00:00Z")
        val setVoiceClip = SetVoiceClipUseCase(
            voiceStore,
            worldPeople,
            campaignPeople,
            locations,
            InstantProvider { later },
        )

        suspend fun insertWorldPerson(): WorldPerson {
            val person = WorldPerson(
                id = "wp-1",
                worldId = "world-1",
                kind = PersonKind.Npc,
                name = "Volo",
                description = "",
                sheet = FifthEditionSheet.empty(),
                createdAt = now,
                updatedAt = now,
            )
            worldPeople.insert(person)
            return person
        }

        suspend fun insertLocation(): Location {
            val location = Location(
                id = "loc-1",
                worldId = "world-1",
                type = LocationType.Place,
                parentLocationId = null,
                name = "Tavern",
                description = "",
                climate = "",
                terrain = "",
                government = "",
                landmarks = emptyList(),
                history = "",
                notes = "",
                createdAt = now,
                updatedAt = now,
            )
            locations.insert(location)
            return location
        }
    }
}

package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNull

internal class ClearVoiceClipUseCaseTest {
    @Test
    fun clearRemovesStoredClip() = runTest {
        val harness = Harness()
        val person = harness.insertWorldPerson()
        val ref = VoiceClipRef.WorldPerson(person.id)
        harness.voiceStore.write(ref, VoiceClipWavFormat.wrapPcm(ByteArray(40)))

        val result = harness.clearVoiceClip(ref)

        assertIs<ClearVoiceClipUseCase.Result.Cleared>(result)
        assertNull(harness.voiceStore.read(ref))
    }

    @Test
    fun clearRejectsMissingPerson() = runTest {
        val harness = Harness()

        val result = harness.clearVoiceClip(VoiceClipRef.WorldPerson("missing"))

        assertIs<ClearVoiceClipUseCase.Result.NotFound>(result)
    }

    private class Harness {
        val worldPeople = FakeWorldPersonRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        val locations = FakeLocationRepository()
        val voiceStore = VoiceClipFileStore(Files.createTempDirectory("ww-voices").toFile())
        val now = Instant.parse("2026-08-29T12:00:00Z")
        val clearVoiceClip = ClearVoiceClipUseCase(
            voiceStore,
            worldPeople,
            campaignPeople,
            locations,
            InstantProvider { Instant.parse("2026-08-31T12:00:00Z") },
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
    }
}

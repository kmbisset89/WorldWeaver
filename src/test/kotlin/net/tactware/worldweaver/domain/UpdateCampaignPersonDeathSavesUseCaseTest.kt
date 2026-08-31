package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class UpdateCampaignPersonDeathSavesUseCaseTest {
    @Test
    fun writesClampedDeathSavesOntoTheSheet() = runTest {
        val harness = Harness()
        harness.insertPerson()

        val result = harness.updateDeathSaves(DeathSaves(successes = 4, failures = -1))

        assertIs<UpdateCampaignPersonDeathSavesUseCase.Result.Updated>(result)
        val sheet = harness.people.getById("pc-1")!!.sheet
        assertEquals(3, sheet.deathSaves.successes)
        assertEquals(0, sheet.deathSaves.failures)
    }

    @Test
    fun missingPersonIsNotFound() = runTest {
        val harness = Harness()

        val result = harness.updateDeathSaves(DeathSaves.none())

        assertIs<UpdateCampaignPersonDeathSavesUseCase.Result.NotFound>(result)
    }

    private class Harness {
        val people = FakeCampaignPersonRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-30T12:00:00Z") }
        private val updateDeathSavesUseCase = UpdateCampaignPersonDeathSavesUseCase(people, instant)

        suspend fun updateDeathSaves(
            deathSaves: DeathSaves,
        ): UpdateCampaignPersonDeathSavesUseCase.Result {
            return updateDeathSavesUseCase("pc-1", deathSaves)
        }

        suspend fun insertPerson() {
            val now = Instant.parse("2026-08-30T12:00:00Z")
            people.insert(
                CampaignPerson(
                    id = "pc-1",
                    campaignId = "campaign-1",
                    worldPersonId = null,
                    kind = PersonKind.PlayerCharacter,
                    name = "Aelar",
                    description = "",
                    sheet = FifthEditionSheet.empty(),
                    overlayHitPoints = null,
                    overlayNotes = "",
                    createdAt = now,
                    updatedAt = now,
                )
            )
        }
    }
}

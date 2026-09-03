package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class StartEncounterUseCaseTest {
    @Test
    fun startMakesEncounterActiveAndResetsTurn() = runTest {
        val harness = Harness()
        val encounter = harness.insertEncounter(
            id = "enc-1",
            status = EncounterStatus.Planned,
            currentRound = 4,
            currentTurnIndex = 2,
        )

        val result = harness.startEncounter(encounter.id)

        assertIs<StartEncounterUseCase.Result.Started>(result)
        val started = harness.encounters.all().single { it.id == "enc-1" }
        assertEquals(EncounterStatus.Active, started.status)
        assertEquals(1, started.currentRound)
        assertEquals(0, started.currentTurnIndex)
    }

    @Test
    fun startResetsAllParticipantEconomy() = runTest {
        val harness = Harness()
        harness.insertEncounter(
            id = "enc-1",
            status = EncounterStatus.Planned,
            currentRound = 4,
            currentTurnIndex = 2,
            participants = listOf(
                harness.spentParticipant("a", "Aelar"),
                harness.spentParticipant("b", "Bram", attacksAllowed = 3),
            ),
        )

        harness.startEncounter("enc-1")

        val started = harness.encounters.getById("enc-1")!!
        val aelar = started.participants.single { it.id == "a" }
        val bram = started.participants.single { it.id == "b" }
        assertEquals(0, aelar.attacksUsed)
        assertEquals(false, aelar.bonusActionUsed)
        assertEquals(false, aelar.reactionUsed)
        assertEquals(2, aelar.attacksAllowed)
        assertEquals(0, bram.attacksUsed)
        assertEquals(false, bram.bonusActionUsed)
        assertEquals(false, bram.reactionUsed)
        assertEquals(3, bram.attacksAllowed)
    }

    @Test
    fun startDeactivatesOtherActiveEncounterInCampaign() = runTest {
        val harness = Harness()
        harness.insertEncounter(id = "enc-old", status = EncounterStatus.Active)
        harness.insertEncounter(id = "enc-new", status = EncounterStatus.Planned)

        harness.startEncounter("enc-new")

        assertEquals(EncounterStatus.Planned, harness.encounters.getById("enc-old")?.status)
        assertEquals(EncounterStatus.Active, harness.encounters.getById("enc-new")?.status)
    }

    private class Harness {
        val encounters = FakeEncounterRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        val startEncounter = StartEncounterUseCase(encounters, instant)

        suspend fun insertEncounter(
            id: String,
            status: EncounterStatus,
            currentRound: Int = 1,
            currentTurnIndex: Int = 0,
            participants: List<EncounterParticipant> = emptyList(),
        ): Encounter {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            val encounter = Encounter(
                id = id,
                campaignId = "campaign-1",
                name = id,
                locationId = null,
                difficulty = EncounterDifficulty.Medium,
                notes = "",
                outcomeNote = "",
                status = status,
                currentRound = currentRound,
                currentTurnIndex = currentTurnIndex,
                participants = participants,
                createdAt = now,
                updatedAt = now,
            )
            encounters.insert(encounter)
            return encounter
        }

        fun spentParticipant(
            id: String,
            name: String,
            attacksAllowed: Int = 2,
        ): EncounterParticipant {
            return EncounterParticipant(
                id = id,
                name = name,
                source = EncounterParticipantSource.Nameless,
                sourceId = null,
                initiativeRoll = 12,
                initiativeBonus = 0,
                armorClass = 10,
                hitPoints = 10,
                maxHitPoints = 10,
                temporaryHitPoints = 0,
                conditions = emptyList(),
                groupCount = 1,
                combatState = CombatState.Conscious,
                attacksAllowed = attacksAllowed,
                attacksUsed = 2,
                bonusActionUsed = true,
                reactionUsed = true,
            )
        }
    }
}

package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class AdvanceEncounterTurnUseCaseTest {
    @Test
    fun nextWrapsAndIncrementsRound() = runTest {
        val harness = Harness()
        harness.insertEncounter(currentTurnIndex = 1, currentRound = 1)

        val result = harness.advance(EncounterTurnDirection.Next)

        assertIs<AdvanceEncounterTurnUseCase.Result.Advanced>(result)
        val encounter = harness.encounters.getById("enc-1")!!
        assertEquals(0, encounter.currentTurnIndex)
        assertEquals(2, encounter.currentRound)
    }

    @Test
    fun nextResetsArrivingEconomyAndLeavesOthers() = runTest {
        val harness = Harness()
        harness.insertEncounter(
            currentTurnIndex = 0,
            currentRound = 1,
            participants = listOf(
                harness.spentParticipant("a", "Aelar", roll = 15),
                harness.spentParticipant("b", "Bram", roll = 10, attacksAllowed = 2),
            ),
        )

        harness.advance(EncounterTurnDirection.Next)

        val encounter = harness.encounters.getById("enc-1")!!
        val aelar = encounter.participants.single { it.id == "a" }
        val bram = encounter.participants.single { it.id == "b" }
        assertEquals(1, encounter.currentTurnIndex)
        assertEquals(2, aelar.attacksUsed)
        assertEquals(true, aelar.bonusActionUsed)
        assertEquals(true, aelar.reactionUsed)
        assertEquals(0, bram.attacksUsed)
        assertEquals(false, bram.bonusActionUsed)
        assertEquals(false, bram.reactionUsed)
        assertEquals(2, bram.attacksAllowed)
    }

    @Test
    fun previousLeavesEconomyUnchanged() = runTest {
        val harness = Harness()
        harness.insertEncounter(
            currentTurnIndex = 1,
            currentRound = 1,
            participants = listOf(
                harness.spentParticipant("a", "Aelar", roll = 15),
                harness.spentParticipant("b", "Bram", roll = 10),
            ),
        )

        harness.advance(EncounterTurnDirection.Previous)

        val encounter = harness.encounters.getById("enc-1")!!
        val aelar = encounter.participants.single { it.id == "a" }
        val bram = encounter.participants.single { it.id == "b" }
        assertEquals(0, encounter.currentTurnIndex)
        assertEquals(2, aelar.attacksUsed)
        assertEquals(true, aelar.bonusActionUsed)
        assertEquals(true, aelar.reactionUsed)
        assertEquals(2, bram.attacksUsed)
        assertEquals(true, bram.bonusActionUsed)
        assertEquals(true, bram.reactionUsed)
    }

    @Test
    fun previousWrapsAndDecrementsRound() = runTest {
        val harness = Harness()
        harness.insertEncounter(currentTurnIndex = 0, currentRound = 3)

        harness.advance(EncounterTurnDirection.Previous)

        val encounter = harness.encounters.getById("enc-1")!!
        assertEquals(1, encounter.currentTurnIndex)
        assertEquals(2, encounter.currentRound)
    }

    private class Harness {
        val encounters = FakeEncounterRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        val advanceTurn = AdvanceEncounterTurnUseCase(encounters, instant)

        suspend fun advance(direction: EncounterTurnDirection): AdvanceEncounterTurnUseCase.Result {
            return advanceTurn("enc-1", direction)
        }

        suspend fun insertEncounter(
            currentTurnIndex: Int,
            currentRound: Int,
            participants: List<EncounterParticipant>? = null,
        ) {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            encounters.insert(
                Encounter(
                    id = "enc-1",
                    campaignId = "campaign-1",
                    name = "Fight",
                    locationId = null,
                    difficulty = EncounterDifficulty.Medium,
                    notes = "",
                    outcomeNote = "",
                    status = EncounterStatus.Active,
                    currentRound = currentRound,
                    currentTurnIndex = currentTurnIndex,
                    participants = participants ?: listOf(
                        participant("a", "Aelar", roll = 15),
                        participant("b", "Bram", roll = 10),
                    ),
                    createdAt = now,
                    updatedAt = now,
                )
            )
        }

        fun spentParticipant(
            id: String,
            name: String,
            roll: Int,
            attacksAllowed: Int = 2,
        ): EncounterParticipant {
            return participant(id, name, roll).copy(
                attacksAllowed = attacksAllowed,
                attacksUsed = 2,
                bonusActionUsed = true,
                reactionUsed = true,
            )
        }

        private fun participant(
            id: String,
            name: String,
            roll: Int,
        ): EncounterParticipant {
            return EncounterParticipant(
                id = id,
                name = name,
                source = EncounterParticipantSource.Nameless,
                sourceId = null,
                initiativeRoll = roll,
                initiativeBonus = 0,
                armorClass = 10,
                hitPoints = 10,
                maxHitPoints = 10,
                temporaryHitPoints = 0,
                conditions = emptyList(),
                groupCount = 1,
                combatState = CombatState.Conscious,
            )
        }
    }
}

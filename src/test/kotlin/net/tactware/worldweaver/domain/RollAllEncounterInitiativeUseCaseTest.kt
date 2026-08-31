package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class RollAllEncounterInitiativeUseCaseTest {
    @Test
    fun rollsEveryMissingParticipantAndPersists() = runTest {
        val harness = Harness()
        harness.insertEncounter(
            participants = listOf(
                harness.participant(id = "a", roll = null),
                harness.participant(id = "b", roll = 6),
            )
        )

        val result = harness.rollAll("enc-1", overwriteExisting = false)

        assertIs<RollAllEncounterInitiativeUseCase.Result.Rolled>(result)
        val rolls = harness.encounters.getById("enc-1")!!.participants.map { it.initiativeRoll }
        assertEquals(listOf(15, 6), rolls)
    }

    @Test
    fun missingEncounterIsNotFound() = runTest {
        val harness = Harness()

        val result = harness.rollAll("missing", overwriteExisting = true)

        assertIs<RollAllEncounterInitiativeUseCase.Result.NotFound>(result)
    }

    @Test
    fun emptyRosterIsNoParticipants() = runTest {
        val harness = Harness()
        harness.insertEncounter(participants = emptyList())

        val result = harness.rollAll("enc-1", overwriteExisting = true)

        assertIs<RollAllEncounterInitiativeUseCase.Result.NoParticipants>(result)
    }

    private class Harness {
        val encounters = FakeEncounterRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-30T12:00:00Z") }
        private val rollAllInitiative = RollAllEncounterInitiativeUseCase(
            encounters,
            instant,
            RollEncounterInitiativeUseCase(DiceRoller { _ -> 15 }),
        )

        suspend fun rollAll(
            encounterId: String,
            overwriteExisting: Boolean,
        ): RollAllEncounterInitiativeUseCase.Result {
            return rollAllInitiative(encounterId, overwriteExisting)
        }

        fun participant(id: String, roll: Int?): EncounterParticipant {
            return EncounterParticipant(
                id = id,
                name = id,
                source = EncounterParticipantSource.Nameless,
                sourceId = null,
                initiativeRoll = roll,
                initiativeBonus = 1,
                armorClass = 13,
                hitPoints = 7,
                maxHitPoints = 7,
                temporaryHitPoints = 0,
                conditions = emptyList(),
                groupCount = 1,
                combatState = CombatState.Conscious,
            )
        }

        suspend fun insertEncounter(participants: List<EncounterParticipant>) {
            val now = Instant.parse("2026-08-30T12:00:00Z")
            encounters.insert(
                Encounter(
                    id = "enc-1",
                    campaignId = "campaign-1",
                    name = "Ambush",
                    locationId = null,
                    difficulty = EncounterDifficulty.Medium,
                    notes = "",
                    outcomeNote = "",
                    status = EncounterStatus.Planned,
                    currentRound = 1,
                    currentTurnIndex = 0,
                    participants = participants,
                    createdAt = now,
                    updatedAt = now,
                )
            )
        }
    }
}

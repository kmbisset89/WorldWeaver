package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

internal class PlaceEncounterTokenUseCaseTest {
    @Test
    fun placePersistsGridCell() = runTest {
        val harness = Harness()
        harness.insertEncounter()

        val result = harness.placeToken(GridCell(column = 3, row = 4))

        val placed = assertIs<PlaceEncounterTokenUseCase.Result.Placed>(result)
        assertEquals(3, placed.participant.gridColumn)
        assertEquals(4, placed.participant.gridRow)
        assertEquals(GridCell(3, 4), harness.participant().boardCell())
    }

    @Test
    fun placeRejectsCellOutsideTheMap() = runTest {
        val harness = Harness()
        harness.insertEncounter()

        val result = harness.placeToken(GridCell(column = 20, row = 0), columns = 10, rows = 10)

        assertIs<PlaceEncounterTokenUseCase.Result.InvalidCell>(result)
        assertNull(harness.participant().boardCell())
    }

    @Test
    fun placeRejectsUnknownParticipant() = runTest {
        val harness = Harness()
        harness.insertEncounter()

        val result = harness.placeToken(
            GridCell(column = 1, row = 1),
            participantId = "missing",
        )

        assertIs<PlaceEncounterTokenUseCase.Result.NotFound>(result)
    }

    @Test
    fun placeRejectsFootprintThatLeavesTheMap() = runTest {
        val harness = Harness()
        harness.insertEncounter()

        val result = harness.placeToken(
            GridCell(column = 9, row = 9),
            columns = 10,
            rows = 10,
            span = 2,
        )

        assertIs<PlaceEncounterTokenUseCase.Result.InvalidCell>(result)
        assertNull(harness.participant().boardCell())
    }

    private class Harness {
        val encounters = FakeEncounterRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        val placeToken = PlaceEncounterTokenUseCase(encounters, instant)

        suspend fun placeToken(
            cell: GridCell,
            participantId: String = "p-1",
            columns: Int = 20,
            rows: Int = 20,
            span: Int = 1,
        ): PlaceEncounterTokenUseCase.Result {
            return placeToken("enc-1", participantId, cell, columns, rows, span)
        }

        fun participant(): EncounterParticipant {
            return encounters.all().single().participants.single { it.id == "p-1" }
        }

        suspend fun insertEncounter() {
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
                    currentRound = 1,
                    currentTurnIndex = 0,
                    participants = listOf(
                        EncounterParticipant(
                            id = "p-1",
                            name = "Goblin",
                            source = EncounterParticipantSource.Nameless,
                            sourceId = null,
                            initiativeRoll = null,
                            initiativeBonus = 1,
                            armorClass = 13,
                            hitPoints = 10,
                            maxHitPoints = 10,
                            temporaryHitPoints = 0,
                            conditions = emptyList(),
                            groupCount = 1,
                            combatState = CombatState.Conscious,
                        )
                    ),
                    createdAt = now,
                    updatedAt = now,
                )
            )
        }
    }
}

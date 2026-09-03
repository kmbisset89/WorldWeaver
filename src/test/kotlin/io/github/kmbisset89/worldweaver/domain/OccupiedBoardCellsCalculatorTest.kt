package io.github.kmbisset89.worldweaver.domain

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class OccupiedBoardCellsCalculatorTest {
    @Test
    fun largeCreatureOccupiesNxNFromOrigin() {
        val now = Instant.parse("2026-09-02T12:00:00Z")
        val person = CampaignPerson(
            id = "pc-1",
            campaignId = "campaign-1",
            worldPersonId = null,
            kind = PersonKind.Monster,
            name = "Ogre",
            description = "",
            sheet = FifthEditionSheet.empty().copy(creatureSize = CreatureSize.Large),
            overlayHitPoints = null,
            overlayNotes = "",
            createdAt = now,
            updatedAt = now,
        )
        val encounter = Encounter(
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
                    name = "Ogre",
                    source = EncounterParticipantSource.CampaignPerson,
                    sourceId = "pc-1",
                    initiativeRoll = 10,
                    initiativeBonus = 0,
                    armorClass = 11,
                    hitPoints = 59,
                    maxHitPoints = 59,
                    temporaryHitPoints = 0,
                    conditions = emptyList(),
                    groupCount = 1,
                    combatState = CombatState.Conscious,
                    gridColumn = 2,
                    gridRow = 3,
                )
            ),
            createdAt = now,
            updatedAt = now,
        )

        val occupied = OccupiedBoardCellsCalculator().occupiedCells(
            encounter = encounter,
            people = PeopleSnapshot(worldPeople = emptyList(), campaignPeople = listOf(person)),
        )

        assertEquals(
            setOf(
                GridCell(2, 3),
                GridCell(3, 3),
                GridCell(2, 4),
                GridCell(3, 4),
            ),
            occupied,
        )
        assertEquals(4, CreatureSize.Large.occupiedCells(GridCell(2, 3)).size)
        assertTrue(CreatureSize.Huge.occupiedCells(GridCell(0, 0)).contains(GridCell(2, 2)))
    }
}

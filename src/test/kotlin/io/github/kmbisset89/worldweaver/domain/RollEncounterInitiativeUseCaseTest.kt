package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class RollEncounterInitiativeUseCaseTest {
    @Test
    fun rollsMissingInitiativeOnlyWhenNotOverwriting() {
        val roller = RollEncounterInitiativeUseCase(DiceRoller { _ -> 14 })
        val missing = participant(id = "a", roll = null)
        val existing = participant(id = "b", roll = 9)

        val rolled = roller(listOf(missing, existing), overwriteExisting = false)

        assertEquals(14, rolled[0].initiativeRoll)
        assertEquals(9, rolled[1].initiativeRoll)
    }

    @Test
    fun overwriteReplacesExistingRolls() {
        val roller = RollEncounterInitiativeUseCase(DiceRoller { _ -> 18 })
        val existing = participant(id = "a", roll = 3)

        val rolled = roller(listOf(existing), overwriteExisting = true)

        assertEquals(18, rolled.single().initiativeRoll)
    }

    @Test
    fun emptyRosterStaysEmpty() {
        val roller = RollEncounterInitiativeUseCase(DiceRoller { _ -> 10 })

        assertEquals(emptyList(), roller(emptyList(), overwriteExisting = true))
        assertNull(roller(emptyList(), overwriteExisting = false).firstOrNull())
    }

    private fun participant(id: String, roll: Int?): EncounterParticipant {
        return EncounterParticipant(
            id = id,
            name = id,
            source = EncounterParticipantSource.Nameless,
            sourceId = null,
            initiativeRoll = roll,
            initiativeBonus = 2,
            armorClass = 13,
            hitPoints = 7,
            maxHitPoints = 7,
            temporaryHitPoints = 0,
            conditions = emptyList(),
            groupCount = 1,
            combatState = CombatState.Conscious,
        )
    }
}

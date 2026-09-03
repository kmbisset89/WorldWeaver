package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class GenerateRandomNpcUseCaseTest {
    @Test
    fun generatorProducesSixScoresInRangeWithNameAndRace() {
        val dice = sequenceOf(3, 4, 5).cycle().iterator()
        val roller = AbilityScoreRoller(DiceRoller { _ -> dice.next() })
        val generate = GenerateRandomNpcUseCase(
            abilityScoreRoller = roller,
            nextNameIndex = { 0 },
            nextRaceIndex = { 0 },
        )

        val draft = generate(AbilityScoreMethod.ThreeD6)

        assertEquals(FifthEditionReference.npcNames.first(), draft.name)
        assertEquals(FifthEditionReference.generatorRaces.first(), draft.race)
        val scores = listOf(
            draft.abilityScores.strength,
            draft.abilityScores.dexterity,
            draft.abilityScores.constitution,
            draft.abilityScores.intelligence,
            draft.abilityScores.wisdom,
            draft.abilityScores.charisma,
        )
        assertEquals(6, scores.size)
        scores.forEach { score ->
            assertTrue(score in 3..18, "score $score was not in 3..18")
        }
        assertEquals(12, draft.abilityScores.strength)
    }

    @Test
    fun fourD6DropLowestUsesHighestThreeDice() {
        val dice = sequenceOf(1, 6, 6, 6).cycle().iterator()
        val roller = AbilityScoreRoller(DiceRoller { _ -> dice.next() })
        val generate = GenerateRandomNpcUseCase(
            abilityScoreRoller = roller,
            nextNameIndex = { 1 },
            nextRaceIndex = { 1 },
        )

        val draft = generate(AbilityScoreMethod.FourD6DropLowest)

        assertEquals(18, draft.abilityScores.strength)
        assertTrue(draft.name.isNotBlank())
        assertTrue(draft.race.isNotBlank())
    }

    private fun <T> Sequence<T>.cycle(): Sequence<T> {
        val values = toList()
        return generateSequence(0) { it + 1 }.map { values[it % values.size] }
    }
}

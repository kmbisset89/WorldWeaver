package io.github.kmbisset89.worldweaver.ui.dice

import io.github.kmbisset89.worldweaver.domain.DiceRollResult
import io.github.kmbisset89.worldweaver.domain.DieSides
import io.github.kmbisset89.worldweaver.domain.RollMode
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class DiceRollFormatTest {
    @Test
    fun keptTwentyIsNaturalTwenty() {
        val result = roll(faces = listOf(20), kept = listOf(20))

        assertTrue(isNaturalTwenty(result))
        assertFalse(isNaturalOne(result))
    }

    @Test
    fun keptOneIsNaturalOne() {
        val result = roll(faces = listOf(1), kept = listOf(1))

        assertTrue(isNaturalOne(result))
        assertFalse(isNaturalTwenty(result))
    }

    @Test
    fun advantageKeepsTwentyOverOne() {
        val result = roll(
            faces = listOf(20, 1),
            kept = listOf(20),
            mode = RollMode.Advantage,
        )

        assertTrue(isNaturalTwenty(result))
        assertFalse(isNaturalOne(result))
    }

    @Test
    fun nonD20IsNeverCritical() {
        val result = roll(sides = 6, faces = listOf(6), kept = listOf(6))

        assertFalse(isNaturalTwenty(result))
        assertFalse(isNaturalOne(result))
        assertFalse(isNaturalTwentyFace(DieSides.D6, 6))
        assertFalse(isNaturalOneFace(DieSides.D6, 1))
    }

    private fun roll(
        sides: Int = 20,
        faces: List<Int>,
        kept: List<Int>,
        mode: RollMode = RollMode.Normal,
    ): DiceRollResult {
        return DiceRollResult(
            sides = sides,
            count = kept.size,
            modifier = 0,
            mode = mode,
            faces = faces,
            keptFaces = kept,
            total = kept.sum(),
        )
    }
}

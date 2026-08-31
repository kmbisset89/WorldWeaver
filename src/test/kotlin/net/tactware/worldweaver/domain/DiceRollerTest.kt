package net.tactware.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class DiceRollerTest {
    @Test
    fun rollsRequestedCountAndAddsModifier() {
        val faces = sequenceOf(2, 5, 3).iterator()
        val roller = DiceRoller { _ -> faces.next() }

        val result = roller.roll(
            DiceRollRequest(sides = 6, count = 3, modifier = 2),
        )

        assertEquals(listOf(2, 5, 3), result.faces)
        assertEquals(listOf(2, 5, 3), result.keptFaces)
        assertEquals(12, result.total)
        assertEquals(RollMode.Normal, result.mode)
        assertEquals(DiceRollSource.Automated, result.source)
    }

    @Test
    fun advantageKeepsHigherOfTwoD20() {
        val faces = sequenceOf(8, 14).iterator()
        val roller = DiceRoller { _ -> faces.next() }

        val result = roller.roll(
            DiceRollRequest(
                sides = DieSides.D20.sides,
                count = 1,
                mode = RollMode.Advantage,
            ),
        )

        assertEquals(listOf(8, 14), result.faces)
        assertEquals(listOf(14), result.keptFaces)
        assertEquals(14, result.total)
        assertEquals(RollMode.Advantage, result.mode)
    }

    @Test
    fun disadvantageKeepsLowerOfTwoD20() {
        val faces = sequenceOf(8, 14).iterator()
        val roller = DiceRoller { _ -> faces.next() }

        val result = roller.roll(
            DiceRollRequest(
                sides = DieSides.D20.sides,
                count = 1,
                mode = RollMode.Disadvantage,
            ),
        )

        assertEquals(listOf(8, 14), result.faces)
        assertEquals(listOf(8), result.keptFaces)
        assertEquals(8, result.total)
        assertEquals(RollMode.Disadvantage, result.mode)
    }

    @Test
    fun advantageOnNonD20FallsBackToNormal() {
        val faces = sequenceOf(3, 4).iterator()
        val roller = DiceRoller { _ -> faces.next() }

        val result = roller.roll(
            DiceRollRequest(
                sides = 6,
                count = 2,
                mode = RollMode.Advantage,
            ),
        )

        assertEquals(listOf(3, 4), result.faces)
        assertEquals(listOf(3, 4), result.keptFaces)
        assertEquals(7, result.total)
        assertEquals(RollMode.Normal, result.mode)
    }

    @Test
    fun d100StaysInRange() {
        val roller = DiceRoller { sides -> sides }

        val result = roller.roll(DiceRollRequest(sides = DieSides.D100.sides, count = 1))

        assertEquals(100, result.sides)
        assertTrue(result.faces.single() in 1..100)
        assertEquals(100, result.total)
    }

    @Test
    fun recordBuildsManualResultFromFaces() {
        val roller = DiceRoller()

        val result = roller.record(
            DiceRollRequest(sides = 6, count = 3, modifier = 2),
            faces = listOf(2, 5, 3),
        )

        assertEquals(listOf(2, 5, 3), result?.faces)
        assertEquals(listOf(2, 5, 3), result?.keptFaces)
        assertEquals(12, result?.total)
        assertEquals(DiceRollSource.Manual, result?.source)
    }

    @Test
    fun recordAdvantageKeepsHigherFace() {
        val roller = DiceRoller()

        val result = roller.record(
            DiceRollRequest(
                sides = DieSides.D20.sides,
                count = 1,
                mode = RollMode.Advantage,
            ),
            faces = listOf(8, 14),
        )

        assertEquals(listOf(8, 14), result?.faces)
        assertEquals(listOf(14), result?.keptFaces)
        assertEquals(14, result?.total)
        assertEquals(DiceRollSource.Manual, result?.source)
    }

    @Test
    fun recordRejectsWrongFaceCount() {
        val roller = DiceRoller()

        val result = roller.record(
            DiceRollRequest(sides = 6, count = 3),
            faces = listOf(2, 5),
        )

        assertEquals(null, result)
    }

    @Test
    fun recordRejectsOutOfRangeFace() {
        val roller = DiceRoller()

        val result = roller.record(
            DiceRollRequest(sides = 6, count = 1),
            faces = listOf(7),
        )

        assertEquals(null, result)
    }
}

package net.tactware.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class DiceNotationParserTest {
    private val parser = DiceNotationParser()

    @Test
    fun parsesBareDie() {
        val result = parser.parse("d20")

        assertEquals(20, result?.sides)
        assertEquals(1, result?.count)
        assertEquals(0, result?.modifier)
        assertEquals(RollMode.Normal, result?.mode)
    }

    @Test
    fun parsesExplicitCount() {
        val result = parser.parse("1d20")

        assertEquals(20, result?.sides)
        assertEquals(1, result?.count)
    }

    @Test
    fun parsesCountAndSides() {
        val result = parser.parse("2d6")

        assertEquals(6, result?.sides)
        assertEquals(2, result?.count)
        assertEquals(0, result?.modifier)
    }

    @Test
    fun parsesPositiveModifier() {
        val result = parser.parse("2d6+3")

        assertEquals(6, result?.sides)
        assertEquals(2, result?.count)
        assertEquals(3, result?.modifier)
    }

    @Test
    fun parsesNegativeModifier() {
        val result = parser.parse("2d6-1")

        assertEquals(6, result?.sides)
        assertEquals(2, result?.count)
        assertEquals(-1, result?.modifier)
    }

    @Test
    fun parsesAdvantageOnSingleD20() {
        val result = parser.parse("d20 adv")

        assertEquals(20, result?.sides)
        assertEquals(1, result?.count)
        assertEquals(RollMode.Advantage, result?.mode)
    }

    @Test
    fun parsesDisadvantageWithModifier() {
        val result = parser.parse("1d20+5 dis")

        assertEquals(20, result?.sides)
        assertEquals(5, result?.modifier)
        assertEquals(RollMode.Disadvantage, result?.mode)
    }

    @Test
    fun allowsSpacesAroundTokens() {
        val result = parser.parse("2d6 + 3")

        assertEquals(6, result?.sides)
        assertEquals(2, result?.count)
        assertEquals(3, result?.modifier)
    }

    @Test
    fun rejectsAdvantageOnNonD20() {
        assertNull(parser.parse("2d6 adv"))
    }

    @Test
    fun rejectsUnknownDie() {
        assertNull(parser.parse("d7"))
    }

    @Test
    fun rejectsEmptyAndIncompleteText() {
        assertNull(parser.parse(""))
        assertNull(parser.parse("2d"))
        assertNull(parser.parse("d"))
    }
}

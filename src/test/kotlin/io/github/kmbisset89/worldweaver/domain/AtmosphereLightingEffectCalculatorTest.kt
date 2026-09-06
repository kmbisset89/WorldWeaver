package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AtmosphereLightingEffectCalculatorTest {
    private val calculator = AtmosphereLightingEffectCalculator()

    @Test
    fun lightningFlashesWhiteThenRestoresTheCurrentLook() {
        val frames = calculator.calculate(AtmosphereLightingEffect.Lightning, TAVERN)
        assertTrue(frames.size >= 4)
        assertEquals(255, frames.first().look.red)
        assertEquals(255, frames.first().look.green)
        assertEquals(255, frames.first().look.blue)
        assertEquals(100, frames.first().look.brightness)
        assertEquals(TAVERN, frames.last().look)
        assertTrue(frames.last().transitionMs > 0)
        assertEquals(0L, frames.last().holdMs)
    }

    @Test
    fun criticalStrikeUsesCombatRedThenRestores() {
        val frames = calculator.calculate(AtmosphereLightingEffect.CriticalStrike, TAVERN)
        assertEquals(196, frames.first().look.red)
        assertEquals(30, frames.first().look.green)
        assertEquals(58, frames.first().look.blue)
        assertEquals(TAVERN, frames.last().look)
    }

    @Test
    fun darknessDimsTheCurrentColorThenRestoresSlowly() {
        val frames = calculator.calculate(AtmosphereLightingEffect.Darkness, TAVERN)
        assertEquals(2, frames.size)
        assertEquals(TAVERN.copy(brightness = 3), frames.first().look)
        assertEquals(TAVERN, frames.last().look)
        assertEquals(900, frames.last().transitionMs)
    }

    @Test
    fun everyEffectRestoresTheStartingLook() {
        AtmosphereLightingEffect.entries.forEach { effect ->
            val frames = calculator.calculate(effect, TAVERN)
            assertEquals(TAVERN, frames.last().look, effect.name)
        }
    }

    private companion object {
        val TAVERN = LightingLook(powerOn = true, brightness = 55, red = 227, green = 155, blue = 90)
    }
}

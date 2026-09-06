package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LightingTransitionCalculatorTest {
    private val calculator = LightingTransitionCalculator()

    @Test
    fun zeroDurationReturnsOnlyTheTargetLook() {
        val frames = calculator.calculate(
            from = WARM,
            to = COMBAT,
            durationMs = 0,
        )
        assertEquals(listOf(LightingTransitionCalculator.Frame(0L, COMBAT)), frames)
    }

    @Test
    fun identicalLooksReturnASingleFrame() {
        val frames = calculator.calculate(
            from = WARM,
            to = WARM,
            durationMs = 400,
        )
        assertEquals(1, frames.size)
        assertEquals(WARM, frames.single().look)
    }

    @Test
    fun easeInOutMidBrightnessIsBelowLinear() {
        val frames = calculator.calculate(
            from = LightingLook(powerOn = false, brightness = 1, red = 0, green = 0, blue = 0),
            to = LightingLook(powerOn = true, brightness = 100, red = 0, green = 0, blue = 0),
            durationMs = 400,
            stepIntervalMs = 100,
            easing = LightingTransitionCalculator.Easing.EaseInOut,
        )
        assertEquals(4, frames.size)
        assertEquals(16, frames.first().look.brightness)
        assertEquals(100, frames.last().look.brightness)
        assertTrue(frames.last().look.powerOn)
        assertEquals(400L, frames.last().delayFromStartMs)
    }

    @Test
    fun linearRgbMidpointIsBrighterThanSrgbAverage() {
        val frames = calculator.calculate(
            from = LightingLook(powerOn = true, brightness = 100, red = 0, green = 0, blue = 0),
            to = LightingLook(powerOn = true, brightness = 100, red = 255, green = 255, blue = 255),
            durationMs = 200,
            stepIntervalMs = 100,
            easing = LightingTransitionCalculator.Easing.Linear,
        )
        val midpoint = frames.first().look
        assertTrue(midpoint.red > 128)
        assertEquals(midpoint.red, midpoint.green)
        assertEquals(midpoint.red, midpoint.blue)
        assertEquals(255, frames.last().look.red)
    }

    @Test
    fun fadeToOffEndsPoweredDown() {
        val frames = calculator.calculate(
            from = WARM,
            to = LightingLook(powerOn = false, brightness = 1, red = 0, green = 0, blue = 0),
            durationMs = 80,
            stepIntervalMs = 80,
        )
        val last = frames.single().look
        assertFalse(last.powerOn)
        assertEquals(1, last.brightness)
    }

    @Test
    fun mixAtZeroAndOneMatchesEndpointBrightness() {
        val start = calculator.mix(WARM, COMBAT, 0.0)
        val end = calculator.mix(WARM, COMBAT, 1.0)
        assertEquals(WARM.brightness, start.brightness)
        assertEquals(COMBAT.brightness, end.brightness)
        assertTrue(start.powerOn)
        assertTrue(end.powerOn)
    }

    private companion object {
        val WARM = LightingLook(powerOn = true, brightness = 55, red = 227, green = 155, blue = 90)
        val COMBAT = LightingLook(powerOn = true, brightness = 90, red = 196, green = 30, blue = 58)
    }
}

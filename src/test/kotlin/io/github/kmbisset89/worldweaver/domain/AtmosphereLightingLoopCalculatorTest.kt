package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AtmosphereLightingLoopCalculatorTest {
    private val calculator = AtmosphereLightingLoopCalculator()

    @Test
    fun waterStaysInBlueRange() {
        val samples = listOf(0L, 900L, 1_800L, 2_700L).map { elapsed ->
            calculator.lookAt(AtmosphereLightingLoop.Water, elapsed)
        }
        samples.forEach { look ->
            assertTrue(look.blue > look.red, "blue=${look.blue} red=${look.red}")
            assertTrue(look.powerOn)
        }
        assertTrue(samples.first() != samples[2])
    }

    @Test
    fun waterRepeatsAfterOnePeriod() {
        val start = calculator.lookAt(AtmosphereLightingLoop.Water, 0L)
        val wrapped = calculator.lookAt(AtmosphereLightingLoop.Water, 3_600L)
        assertEquals(start, wrapped)
    }

    @Test
    fun fireOscillatesThroughWarmColors() {
        val start = calculator.lookAt(AtmosphereLightingLoop.Fire, 0L)
        val mid = calculator.lookAt(AtmosphereLightingLoop.Fire, 1_200L)
        assertTrue(start.red > start.blue)
        assertTrue(mid.red > mid.blue)
        assertTrue(start.red != mid.red || start.green != mid.green || start.brightness != mid.brightness)
        val wrapped = calculator.lookAt(AtmosphereLightingLoop.Fire, 2_400L)
        assertEquals(start.red, wrapped.red)
        assertEquals(start.green, wrapped.green)
        assertEquals(start.blue, wrapped.blue)
    }

    @Test
    fun everyLoopProducesAPoweredLook() {
        AtmosphereLightingLoop.entries.forEach { loop ->
            val look = calculator.lookAt(loop, 400L)
            assertTrue(look.powerOn, loop.name)
            assertTrue(look.brightness in 1..100, loop.name)
        }
    }
}

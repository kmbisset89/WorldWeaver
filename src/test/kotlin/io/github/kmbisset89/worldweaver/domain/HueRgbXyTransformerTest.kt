package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class HueRgbXyTransformerTest {
    private val transformer = HueRgbXyTransformer()

    @Test
    fun mapsBlackToD65WhitePoint() {
        val xy = transformer.transform(0, 0, 0)
        assertEquals(0.3127, xy.x, 0.0001)
        assertEquals(0.3290, xy.y, 0.0001)
    }

    @Test
    fun mapsRedTowardHigherXThanBlue() {
        val red = transformer.transform(196, 30, 58)
        val blue = transformer.transform(58, 95, 138)
        assertTrue(red.x > blue.x)
        assertTrue(red.x in 0.0..1.0)
        assertTrue(red.y in 0.0..1.0)
    }
}

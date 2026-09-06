package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class AtmosphereSceneTest {
    @Test
    fun summarizesConfiguredTargets() {
        val scene = AtmosphereScene(
            id = "s1",
            name = "Combat",
            entityId = "scene.combat",
            sortOrder = 0,
            hueSceneId = "hue-combat",
            hueLightIds = listOf("light-1", "light-2"),
            goveeDeviceIds = listOf("AA:BB", "CC:DD"),
        )
        assertTrue(scene.hasAnyTarget)
        assertEquals("scene.combat · Hue hue-combat · 2 lights · 2 Govee", scene.targetSummary())
    }

    @Test
    fun hueLightsWithoutSceneCountAsATarget() {
        val scene = AtmosphereScene(
            id = "s1",
            name = "Warm",
            sortOrder = 0,
            hueLightIds = listOf("light-1", "light-2"),
            goveeColorHex = "#E39B5A",
        )
        assertTrue(scene.hasHue)
        assertTrue(scene.hasAnyTarget)
        assertEquals("2 Hue", scene.targetSummary())
    }

    @Test
    fun emptyTargetsHaveNoSummary() {
        val scene = AtmosphereScene(id = "s1", name = "Empty", sortOrder = 0)
        assertFalse(scene.hasAnyTarget)
        assertEquals("", scene.targetSummary())
    }
}

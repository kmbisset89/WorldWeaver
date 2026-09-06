package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class HueClipV2LightColorCommandTransformerTest {
    private val transformer = HueClipV2LightColorCommandTransformer()

    @Test
    fun buildsOnColorBodyForEachLight() {
        val commands = transformer.transform(
            lightIds = listOf("light-1", "light-1", " light-2 "),
            powerOn = true,
            brightness = 90,
            xy = HueRgbXyTransformer.Xy(0.64, 0.33),
        )
        assertEquals(listOf("light-1", "light-2"), commands.map { it.lightId })
        assertTrue(commands.first().bodyJson.contains("\"x\":0.64"))
        assertTrue(commands.first().bodyJson.contains("\"brightness\":90.0"))
        assertTrue(commands.first().bodyJson.contains("\"duration\":0"))
        assertFalse(commands.first().bodyJson.contains("no_effect"))
    }

    @Test
    fun includesRequestedTransitionDuration() {
        val command = transformer.transform(
            lightIds = listOf("light-1"),
            powerOn = true,
            brightness = 55,
            xy = HueRgbXyTransformer.Xy(0.5, 0.4),
            transitionDurationMs = 1_200,
        ).single()
        assertTrue(command.bodyJson.contains("\"duration\":1200"))
    }

    @Test
    fun fadeOffIncludesDynamicsDuration() {
        val command = transformer.transform(
            lightIds = listOf("light-1"),
            powerOn = false,
            brightness = 1,
            xy = HueRgbXyTransformer.Xy(0.3, 0.3),
            transitionDurationMs = 400,
        ).single()
        assertTrue(command.bodyJson.contains("\"on\":false"))
        assertTrue(command.bodyJson.contains("\"duration\":400"))
    }

    @Test
    fun buildsOffBodyWithoutColor() {
        val command = transformer.transform(
            lightIds = listOf("light-1"),
            powerOn = false,
            brightness = 1,
            xy = HueRgbXyTransformer.Xy(0.3, 0.3),
        ).single()
        assertEquals("""{"on":{"on":false}}""", command.bodyJson)
    }
}

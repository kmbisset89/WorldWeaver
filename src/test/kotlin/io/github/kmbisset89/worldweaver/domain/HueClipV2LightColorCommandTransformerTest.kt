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

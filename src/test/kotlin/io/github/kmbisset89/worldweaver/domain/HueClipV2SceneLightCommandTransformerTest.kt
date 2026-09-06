package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class HueClipV2SceneLightCommandTransformerTest {
    private val transformer = HueClipV2SceneLightCommandTransformer()

    @Test
    fun usesXyWhenMirekIsNullAndDropsNoEffect() {
        val commands = transformer.transform(sceneJson(COLORED_ACTION))
        val command = commands.single()
        assertEquals(LIGHT_ID, command.lightId)
        val body = parse(command.bodyJson)
        assertEquals(true, body.booleanAt("on", "on"))
        assertEquals(70.0, body.objectAt("dimming")?.get("brightness")?.jsonPrimitive?.doubleOrNull)
        assertEquals(0.64, body.objectAt("color", "xy")?.get("x")?.jsonPrimitive?.doubleOrNull)
        assertEquals(0.33, body.objectAt("color", "xy")?.get("y")?.jsonPrimitive?.doubleOrNull)
        assertNull(body["color_temperature"])
        assertNull(body["effects"])
        assertFalse(command.bodyJson.contains("no_effect"))
    }

    @Test
    fun usesColorTemperatureWhenMirekIsPresent() {
        val commands = transformer.transform(sceneJson(WHITE_ACTION))
        val body = parse(commands.single().bodyJson)
        assertEquals(370.0, body.objectAt("color_temperature")?.get("mirek")?.jsonPrimitive?.doubleOrNull)
        assertNull(body["color"])
        assertNull(body["effects"])
    }

    @Test
    fun keepsNamedEffectAndGradient() {
        val commands = transformer.transform(sceneJson(FIRE_GRADIENT_ACTION))
        val body = parse(commands.single().bodyJson)
        assertEquals("fire", body.objectAt("effects")?.get("effect")?.jsonPrimitive?.content)
        assertTrue(body.containsKey("gradient"))
    }

    @Test
    fun skipsGroupedLightTargets() {
        val json = """
            {
              "data": [{
                "actions": [{
                  "target": {"rid": "group-1", "rtype": "grouped_light"},
                  "action": {"dimming": {"brightness": 40}}
                }]
              }]
            }
        """.trimIndent()
        assertEquals(emptyList(), transformer.transform(json))
    }

    private fun parse(bodyJson: String): JsonObject {
        return Json.parseToJsonElement(bodyJson).jsonObject
    }

    private fun JsonObject.objectAt(vararg path: String): JsonObject? {
        var current: JsonObject? = this
        path.forEach { key ->
            current = current?.get(key) as? JsonObject
        }
        return current
    }

    private fun JsonObject.booleanAt(vararg path: String): Boolean? {
        val keys = path.toList()
        val parent = objectAt(*keys.dropLast(1).toTypedArray()) ?: return null
        return parent[keys.last()]?.jsonPrimitive?.content?.toBooleanStrictOrNull()
    }

    private companion object {
        const val LIGHT_ID = "11111111-1111-1111-1111-111111111111"
        const val COLORED_ACTION = """
            {
              "on": {"on": true},
              "dimming": {"brightness": 70.0},
              "color": {"xy": {"x": 0.64, "y": 0.33}},
              "color_temperature": {"mirek": null},
              "effects": {"effect": "no_effect"}
            }
        """
        const val WHITE_ACTION = """
            {
              "on": {"on": true},
              "dimming": {"brightness": 80.0},
              "color": {"xy": {"x": 0.3, "y": 0.3}},
              "color_temperature": {"mirek": 370},
              "effects": {"effect": "no_effect"}
            }
        """
        const val FIRE_GRADIENT_ACTION = """
            {
              "on": {"on": true},
              "gradient": {"points": [{"color": {"xy": {"x": 0.5, "y": 0.4}}}]},
              "effects": {"effect": "fire"}
            }
        """

        fun sceneJson(action: String): String {
            return """
                {
                  "errors": [],
                  "data": [{
                    "id": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
                    "actions": [{
                      "target": {"rid": "$LIGHT_ID", "rtype": "light"},
                      "action": $action
                    }]
                  }]
                }
            """.trimIndent()
        }
    }
}

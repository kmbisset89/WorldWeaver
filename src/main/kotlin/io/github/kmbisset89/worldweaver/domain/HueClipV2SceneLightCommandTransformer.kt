package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * Maps a CLIP v2 scene GET payload to per-light PUTs that apply color and brightness.
 *
 * Hue scene actions often include `no_effect` and both `color` and `color_temperature`.
 * Replaying those fields as-is changes brightness but leaves chromaticity stuck.
 */
internal class HueClipV2SceneLightCommandTransformer {
    fun transform(jsonText: String): List<HueClipV2LightCommand> {
        return sceneObjects(jsonText).flatMap { scene ->
            val actions = try {
                scene["actions"]?.jsonArray ?: return@flatMap emptyList()
            } catch (_: Exception) {
                return@flatMap emptyList()
            }
            actions.mapNotNull { element -> commandFrom(element as? JsonObject ?: return@mapNotNull null) }
        }
    }

    private fun commandFrom(entry: JsonObject): HueClipV2LightCommand? {
        val target = entry["target"] as? JsonObject ?: return null
        if (stringContent(target["rtype"]) != LIGHT_TYPE) {
            return null
        }
        val lightId = stringContent(target["rid"])?.trim().orEmpty()
        if (lightId.isEmpty()) {
            return null
        }
        val action = entry["action"] as? JsonObject ?: return null
        val body = sanitizedBody(action) ?: return null
        return HueClipV2LightCommand(lightId = lightId, bodyJson = body.toString())
    }

    private fun sanitizedBody(action: JsonObject): JsonObject? {
        val body = buildJsonObject {
            copyOn(action)
            copyDimming(action)
            copyColorOrTemperature(action)
            copyGradient(action)
            copyNamedEffect(action)
        }
        return body.takeIf { it.isNotEmpty() }
    }

    private fun JsonObjectBuilder.copyOn(action: JsonObject) {
        val onValue = ((action["on"] as? JsonObject)?.get("on") as? JsonPrimitive)?.booleanOrNull ?: return
        putJsonObject("on") { put("on", onValue) }
    }

    private fun JsonObjectBuilder.copyDimming(action: JsonObject) {
        val brightness = numberValue((action["dimming"] as? JsonObject)?.get("brightness")) ?: return
        putJsonObject("dimming") { put("brightness", brightness) }
    }

    private fun JsonObjectBuilder.copyColorOrTemperature(action: JsonObject) {
        val mirek = numberValue((action["color_temperature"] as? JsonObject)?.get("mirek"))
        if (mirek != null) {
            putJsonObject("color_temperature") { put("mirek", mirek.toInt()) }
            return
        }
        val xy = (action["color"] as? JsonObject)?.get("xy") as? JsonObject ?: return
        val x = numberValue(xy["x"]) ?: return
        val y = numberValue(xy["y"]) ?: return
        putJsonObject("color") {
            putJsonObject("xy") {
                put("x", x)
                put("y", y)
            }
        }
    }

    private fun JsonObjectBuilder.copyGradient(action: JsonObject) {
        val gradient = action["gradient"] as? JsonObject ?: return
        if (gradient.isEmpty()) {
            return
        }
        put("gradient", gradient)
    }

    private fun JsonObjectBuilder.copyNamedEffect(action: JsonObject) {
        val effect = namedEffect(action["effects"] as? JsonObject)
        if (effect != null) {
            putJsonObject("effects") { put("effect", effect) }
            return
        }
        val effectsV2 = action["effects_v2"] as? JsonObject ?: return
        val v2Effect = namedEffect(effectsV2["action"] as? JsonObject) ?: namedEffect(effectsV2)
        if (v2Effect != null) {
            putJsonObject("effects") { put("effect", v2Effect) }
        }
    }

    private fun namedEffect(source: JsonObject?): String? {
        val effect = stringContent(source?.get("effect"))?.trim()?.lowercase().orEmpty()
        if (effect.isEmpty() || effect in OMITTED_EFFECTS) {
            return null
        }
        return if (effect in NAMED_EFFECTS) effect else null
    }

    private fun sceneObjects(jsonText: String): List<JsonObject> {
        val root = try {
            json.parseToJsonElement(jsonText).jsonObject
        } catch (_: Exception) {
            return emptyList()
        }
        val data = try {
            root["data"]?.jsonArray
        } catch (_: Exception) {
            null
        }
        if (data != null) {
            return data.mapNotNull { it as? JsonObject }
        }
        return if (root.containsKey("actions")) listOf(root) else emptyList()
    }

    private fun stringContent(element: JsonElement?): String? {
        val primitive = element as? JsonPrimitive ?: return null
        return primitive.contentOrNull
    }

    private fun numberValue(element: JsonElement?): Double? {
        val primitive = element as? JsonPrimitive ?: return null
        return primitive.doubleOrNull ?: primitive.intOrNull?.toDouble()
    }

    private companion object {
        const val LIGHT_TYPE = "light"
        val OMITTED_EFFECTS = setOf("no_effect", "none", "null")
        val NAMED_EFFECTS = setOf(
            "candle",
            "fire",
            "sparkle",
            "prism",
            "opal",
            "glisten",
            "cosmos",
            "sunbeam",
            "enchant",
            "underline",
        )
        val json = Json { ignoreUnknownKeys = true }
    }
}

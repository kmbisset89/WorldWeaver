package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * Builds CLIP v2 light PUTs for a shared on/brightness/xy look.
 */
internal class HueClipV2LightColorCommandTransformer {
    fun transform(
        lightIds: List<String>,
        powerOn: Boolean,
        brightness: Int,
        xy: HueRgbXyTransformer.Xy,
    ): List<HueClipV2LightCommand> {
        val body = if (powerOn) {
            buildJsonObject {
                putJsonObject("on") { put("on", true) }
                putJsonObject("dimming") { put("brightness", brightness.coerceIn(1, 100).toDouble()) }
                putJsonObject("color") {
                    putJsonObject("xy") {
                        put("x", xy.x)
                        put("y", xy.y)
                    }
                }
                putJsonObject("dynamics") { put("duration", 0) }
            }.toString()
        } else {
            """{"on":{"on":false}}"""
        }
        return lightIds.map { it.trim() }.filter { it.isNotEmpty() }.distinct().map { lightId ->
            HueClipV2LightCommand(lightId = lightId, bodyJson = body)
        }
    }
}

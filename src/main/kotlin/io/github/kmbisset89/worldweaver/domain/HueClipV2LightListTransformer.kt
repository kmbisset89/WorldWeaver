package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * Maps a Hue CLIP v2 `/resource/light` payload to catalog entries.
 */
internal class HueClipV2LightListTransformer {
    fun transform(jsonText: String): List<HueLight> {
        val root = try {
            json.parseToJsonElement(jsonText).jsonObject
        } catch (_: Exception) {
            return emptyList()
        }
        val data = try {
            root["data"]?.jsonArray ?: return emptyList()
        } catch (_: Exception) {
            return emptyList()
        }
        return data.mapNotNull { element ->
            val light = element as? JsonObject ?: return@mapNotNull null
            val id = stringContent(light["id"])?.trim().orEmpty()
            if (id.isEmpty()) {
                return@mapNotNull null
            }
            val metadata = light["metadata"] as? JsonObject
            val name = stringContent(metadata?.get("name"))?.trim().orEmpty().ifBlank { id }
            HueLight(id = id, name = name)
        }.sortedBy { it.name.lowercase() }
    }

    private fun stringContent(element: JsonElement?): String? {
        val primitive = element as? JsonPrimitive ?: return null
        return primitive.contentOrNull
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}

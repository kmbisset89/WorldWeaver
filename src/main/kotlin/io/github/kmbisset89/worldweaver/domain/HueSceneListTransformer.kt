package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * Maps a Hue `/scenes` JSON object to catalog entries.
 */
internal class HueSceneListTransformer {
    fun transform(jsonText: String): List<HueScene>? {
        val element = json.parseToJsonElement(jsonText)
        val asArray = try {
            element.jsonArray
        } catch (_: Exception) {
            null
        }
        if (asArray != null) {
            return null
        }
        val obj = try {
            element.jsonObject
        } catch (_: Exception) {
            return emptyList()
        }
        return obj.mapNotNull { (id, value) ->
            val scene = value as? JsonObject ?: return@mapNotNull null
            val name = stringContent(scene["name"])?.trim().orEmpty().ifBlank { id }
            val groupId = stringContent(scene["group"])?.trim().orEmpty().ifBlank { "0" }
            HueScene(id = id, name = name, groupId = groupId)
        }.sortedBy { it.name.lowercase() }
    }

    private fun stringContent(element: kotlinx.serialization.json.JsonElement?): String? {
        val primitive = element as? JsonPrimitive ?: return null
        return primitive.contentOrNull
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}

package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * Maps a Home Assistant `/api/states` JSON payload to `scene.*` catalog entries.
 */
internal class HomeAssistantSceneListTransformer {
    fun transform(jsonText: String): List<HomeAssistantScene> {
        val element = json.parseToJsonElement(jsonText)
        val array = try {
            element.jsonArray
        } catch (_: Exception) {
            return emptyList()
        }
        return array.mapNotNull { item ->
            val obj = try {
                item.jsonObject
            } catch (_: Exception) {
                return@mapNotNull null
            }
            val entityId = stringContent(obj["entity_id"]) ?: return@mapNotNull null
            if (!entityId.startsWith(SCENE_PREFIX)) {
                return@mapNotNull null
            }
            val attributes = obj["attributes"] as? JsonObject
            val friendlyName = stringContent(attributes?.get("friendly_name"))?.trim().orEmpty()
            val name = friendlyName.ifBlank {
                entityId.removePrefix(SCENE_PREFIX).replace('_', ' ')
            }
            HomeAssistantScene(entityId = entityId, name = name)
        }.sortedBy { it.name.lowercase() }
    }

    private fun stringContent(element: JsonElement?): String? {
        val primitive = element as? JsonPrimitive ?: return null
        return primitive.contentOrNull
    }

    private companion object {
        const val SCENE_PREFIX = "scene."

        val json = Json {
            ignoreUnknownKeys = true
        }
    }
}

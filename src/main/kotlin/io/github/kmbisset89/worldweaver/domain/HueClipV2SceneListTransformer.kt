package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * Maps a Hue CLIP v2 `/resource/scene` payload to catalog entries.
 */
internal class HueClipV2SceneListTransformer {
    fun transform(jsonText: String): List<HueScene> {
        return parseEntries(jsonText).map { entry ->
            HueScene(id = entry.id, name = entry.name, groupId = entry.groupId)
        }.sortedBy { it.name.lowercase() }
    }

    /**
     * Returns the CLIP v2 scene id for [requestedId], which may already be a v2 id
     * or a CLIP v1 scene id from an older mapping.
     */
    fun resolveId(jsonText: String, requestedId: String): String? {
        val wanted = requestedId.trim()
        if (wanted.isEmpty()) {
            return null
        }
        return parseEntries(jsonText).firstOrNull { entry ->
            entry.id.equals(wanted, ignoreCase = true) ||
                entry.idV1 == wanted ||
                entry.idV1.endsWith("/$wanted")
        }?.id
    }

    private fun parseEntries(jsonText: String): List<SceneEntry> {
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
            val scene = element as? JsonObject ?: return@mapNotNull null
            val id = stringContent(scene["id"])?.trim().orEmpty()
            if (id.isEmpty()) {
                return@mapNotNull null
            }
            val metadata = scene["metadata"] as? JsonObject
            val name = stringContent(metadata?.get("name"))?.trim().orEmpty().ifBlank { id }
            val group = scene["group"] as? JsonObject
            val groupId = stringContent(group?.get("rid"))?.trim().orEmpty()
            val idV1 = stringContent(scene["id_v1"])?.trim().orEmpty()
            SceneEntry(id = id, name = name, groupId = groupId, idV1 = idV1)
        }
    }

    private fun stringContent(element: JsonElement?): String? {
        val primitive = element as? JsonPrimitive ?: return null
        return primitive.contentOrNull
    }

    private data class SceneEntry(
        val id: String,
        val name: String,
        val groupId: String,
        val idV1: String,
    )

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}

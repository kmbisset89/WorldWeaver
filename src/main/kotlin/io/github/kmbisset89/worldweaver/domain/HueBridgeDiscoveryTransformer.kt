package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * Maps Philips discovery JSON to local Hue bridge addresses.
 */
internal class HueBridgeDiscoveryTransformer {
    fun transform(jsonText: String): List<HueBridge> {
        val element = json.parseToJsonElement(jsonText)
        val array = try {
            element.jsonArray
        } catch (_: Exception) {
            return emptyList()
        }
        return array.mapNotNull { item ->
            val obj = item as? JsonObject ?: return@mapNotNull null
            val ip = stringContent(obj["internalipaddress"])?.trim().orEmpty()
            if (ip.isEmpty()) {
                return@mapNotNull null
            }
            val id = stringContent(obj["id"])?.trim().orEmpty().ifBlank { ip }
            HueBridge(bridgeId = id, ipAddress = ip)
        }
    }

    private fun stringContent(element: kotlinx.serialization.json.JsonElement?): String? {
        val primitive = element as? JsonPrimitive ?: return null
        return primitive.contentOrNull
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}

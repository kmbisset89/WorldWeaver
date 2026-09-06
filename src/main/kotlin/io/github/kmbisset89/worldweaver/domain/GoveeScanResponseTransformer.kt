package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject

/**
 * Maps a Govee LAN scan datagram to a device record.
 */
internal class GoveeScanResponseTransformer {
    fun transform(jsonText: String): GoveeDevice? {
        val root = try {
            json.parseToJsonElement(jsonText).jsonObject
        } catch (_: Exception) {
            return null
        }
        val msg = root["msg"] as? JsonObject ?: return null
        val data = msg["data"] as? JsonObject ?: return null
        val ip = stringContent(data["ip"])?.trim().orEmpty()
        val deviceId = stringContent(data["device"])?.trim().orEmpty()
        if (ip.isEmpty() || deviceId.isEmpty()) {
            return null
        }
        val sku = stringContent(data["sku"])?.trim().orEmpty()
        return GoveeDevice(deviceId = deviceId, ipAddress = ip, sku = sku)
    }

    private fun stringContent(element: kotlinx.serialization.json.JsonElement?): String? {
        val primitive = element as? JsonPrimitive ?: return null
        return primitive.contentOrNull
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}

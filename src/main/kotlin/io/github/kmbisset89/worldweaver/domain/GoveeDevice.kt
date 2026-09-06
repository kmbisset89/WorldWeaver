package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.Serializable

/**
 * A Govee light discovered on the local LAN.
 */
@Serializable
internal data class GoveeDevice(
    val deviceId: String,
    val ipAddress: String,
    val sku: String,
) {
    val displayName: String
        get() {
            val suffix = deviceId.takeLast(5).ifBlank { deviceId }
            return if (sku.isBlank()) suffix else "$sku · $suffix"
        }
}

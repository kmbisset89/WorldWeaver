package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.Serializable

/**
 * A named color, brightness, and power look saved on this computer for Hue and Govee lights.
 */
@Serializable
internal data class AtmosphereMood(
    val id: String,
    val name: String,
    val powerOn: Boolean,
    val brightness: Int,
    val colorHex: String,
    val sortOrder: Int,
) {
    fun matchesLook(colorHex: String, brightness: String, powerOn: Boolean): Boolean {
        val parsedBrightness = brightness.toIntOrNull() ?: return false
        return colorHex.equals(this.colorHex, ignoreCase = true) &&
            parsedBrightness == this.brightness &&
            powerOn == this.powerOn
    }
}

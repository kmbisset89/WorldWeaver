package io.github.kmbisset89.worldweaver.domain

/**
 * Power, brightness, and sRGB color applied to table lights.
 */
internal data class LightingLook(
    val powerOn: Boolean,
    val brightness: Int,
    val red: Int,
    val green: Int,
    val blue: Int,
) {
    /**
     * `#RRGGBB` for the current sRGB channels.
     */
    fun toColorHex(): String {
        return "#%02X%02X%02X".format(
            red.coerceIn(0, 255),
            green.coerceIn(0, 255),
            blue.coerceIn(0, 255),
        )
    }
}

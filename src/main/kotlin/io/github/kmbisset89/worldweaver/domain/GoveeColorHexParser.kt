package io.github.kmbisset89.worldweaver.domain

/**
 * Parses `#RRGGBB` or `RRGGBB` color values for Hue and Govee lighting looks.
 */
internal class GoveeColorHexParser {
    data class Rgb(
        val red: Int,
        val green: Int,
        val blue: Int,
    )

    fun parse(raw: String): Rgb? {
        val hex = raw.trim().removePrefix("#")
        if (hex.length != 6) {
            return null
        }
        return try {
            Rgb(
                red = hex.substring(0, 2).toInt(16),
                green = hex.substring(2, 4).toInt(16),
                blue = hex.substring(4, 6).toInt(16),
            )
        } catch (_: Exception) {
            null
        }
    }
}

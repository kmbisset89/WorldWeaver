package io.github.kmbisset89.worldweaver.domain

/**
 * Built-in table moods a GM can apply to Hue and Govee lights.
 */
internal enum class GoveeLightingPreset(
    val displayName: String,
    val powerOn: Boolean,
    val brightness: Int,
    val colorHex: String,
) {
    WARM("Warm", true, 55, "#E39B5A"),
    CAMPFIRE("Campfire", true, 48, "#E05A1C"),
    DAWN("Dawn", true, 70, "#F0C27A"),
    FOREST("Forest", true, 45, "#3D6B4F"),
    DUNGEON("Dungeon", true, 22, "#6B5344"),
    NIGHT("Night", true, 25, "#3A5F8A"),
    STORM("Storm", true, 50, "#4A4E8A"),
    COMBAT("Combat", true, 90, "#C41E3A"),
    OFF("Off", false, 1, "#000000"),
    ;

    fun matchesLook(colorHex: String, brightness: String, powerOn: Boolean): Boolean {
        val parsedBrightness = brightness.toIntOrNull() ?: return false
        return colorHex.equals(this.colorHex, ignoreCase = true) &&
            parsedBrightness == this.brightness &&
            powerOn == this.powerOn
    }
}

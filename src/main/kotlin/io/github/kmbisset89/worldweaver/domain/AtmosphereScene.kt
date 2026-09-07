package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.Serializable

/**
 * A named table-side mapping that can fire Home Assistant, Philips Hue, and Govee lighting together.
 *
 * Provider ids are specific to the GM's house, not a shareable world.
 */
@Serializable
internal data class AtmosphereScene(
    val id: String,
    val name: String,
    val entityId: String = "",
    val sortOrder: Int,
    val hueSceneId: String = "",
    val hueGroupId: String = "",
    val hueLightIds: List<String> = emptyList(),
    val goveeDeviceIds: List<String> = emptyList(),
    val goveePowerOn: Boolean = true,
    val goveeBrightness: Int = 80,
    val goveeColorHex: String = "",
    val musicTrackId: String = "",
) {
    val hasHomeAssistant: Boolean
        get() = entityId.isNotBlank()

    val hasHue: Boolean
        get() = hueSceneId.isNotBlank() || hueLightIds.isNotEmpty()

    val hasGovee: Boolean
        get() = goveeDeviceIds.isNotEmpty()

    val hasAnyTarget: Boolean
        get() = hasHomeAssistant || hasHue || hasGovee

    fun targetSummary(): String {
        val parts = buildList {
            if (hasHomeAssistant) add(entityId)
            if (hasHue) {
                add(
                    when {
                        hueSceneId.isNotBlank() && hueLightIds.isNotEmpty() ->
                            "Hue $hueSceneId · ${hueLightIds.size} lights"
                        hueSceneId.isNotBlank() -> "Hue $hueSceneId"
                        else -> "${hueLightIds.size} Hue"
                    },
                )
            }
            if (hasGovee) add("${goveeDeviceIds.size} Govee")
        }
        return parts.joinToString(" · ")
    }
}

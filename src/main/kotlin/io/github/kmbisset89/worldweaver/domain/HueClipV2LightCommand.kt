package io.github.kmbisset89.worldweaver.domain

/**
 * A CLIP v2 light PUT that sets one lamp's power, brightness, and color.
 */
internal data class HueClipV2LightCommand(
    val lightId: String,
    val bodyJson: String,
)

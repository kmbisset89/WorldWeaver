package io.github.kmbisset89.worldweaver.domain

/**
 * A scene stored on a Philips Hue bridge.
 */
internal data class HueScene(
    val id: String,
    val name: String,
    val groupId: String,
)

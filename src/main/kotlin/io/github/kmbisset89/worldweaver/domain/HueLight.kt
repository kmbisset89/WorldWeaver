package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.Serializable

/**
 * A lamp on a paired Philips Hue bridge.
 */
@Serializable
internal data class HueLight(
    val id: String,
    val name: String,
)

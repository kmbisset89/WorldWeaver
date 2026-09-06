package io.github.kmbisset89.worldweaver.domain

/**
 * Color and power applied to selected Govee lights.
 */
internal data class GoveeLightCommand(
    val powerOn: Boolean,
    val brightness: Int,
    val red: Int,
    val green: Int,
    val blue: Int,
)

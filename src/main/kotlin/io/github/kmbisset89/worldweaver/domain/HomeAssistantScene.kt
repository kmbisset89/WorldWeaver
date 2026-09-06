package io.github.kmbisset89.worldweaver.domain

/**
 * A `scene.*` entity advertised by Home Assistant.
 */
internal data class HomeAssistantScene(
    val entityId: String,
    val name: String,
)

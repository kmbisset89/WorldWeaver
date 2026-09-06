package io.github.kmbisset89.worldweaver.domain

/**
 * A Hue bridge advertised by Philips' discovery service.
 */
internal data class HueBridge(
    val bridgeId: String,
    val ipAddress: String,
)

package io.github.kmbisset89.worldweaver.domain

/**
 * Local Philips Hue bridge host and application key from link-button pairing.
 */
internal data class HueConnection(
    val bridgeHost: String,
    val applicationKey: String,
) {
    val isConfigured: Boolean
        get() = bridgeHost.isNotBlank() && applicationKey.isNotBlank()
}

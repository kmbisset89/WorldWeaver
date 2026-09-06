package io.github.kmbisset89.worldweaver.domain

/**
 * Credentials for a local Home Assistant instance.
 *
 * [baseUrl] has no trailing slash. [token] is a long-lived access token.
 */
internal data class HomeAssistantConnection(
    val baseUrl: String,
    val token: String,
) {
    val isConfigured: Boolean
        get() = baseUrl.isNotBlank() && token.isNotBlank()
}

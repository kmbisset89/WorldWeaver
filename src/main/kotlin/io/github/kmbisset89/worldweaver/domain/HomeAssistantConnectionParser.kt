package io.github.kmbisset89.worldweaver.domain

import java.net.URI

/**
 * Validates and normalizes a Home Assistant base URL and long-lived access token.
 */
internal class HomeAssistantConnectionParser {
    sealed interface Result {
        data class Valid(val connection: HomeAssistantConnection) : Result
        data object BlankUrl : Result
        data object BlankToken : Result
        data object InvalidUrl : Result
    }

    fun parse(baseUrl: String, token: String): Result {
        val trimmedUrl = baseUrl.trim()
        val trimmedToken = token.trim()
        if (trimmedUrl.isEmpty()) {
            return Result.BlankUrl
        }
        if (trimmedToken.isEmpty()) {
            return Result.BlankToken
        }
        val normalized = trimmedUrl.trimEnd('/')
        val uri = try {
            URI(normalized)
        } catch (_: Exception) {
            return Result.InvalidUrl
        }
        val scheme = uri.scheme?.lowercase()
        if (scheme != "http" && scheme != "https") {
            return Result.InvalidUrl
        }
        if (uri.host.isNullOrBlank()) {
            return Result.InvalidUrl
        }
        return Result.Valid(
            HomeAssistantConnection(
                baseUrl = normalized,
                token = trimmedToken,
            ),
        )
    }
}

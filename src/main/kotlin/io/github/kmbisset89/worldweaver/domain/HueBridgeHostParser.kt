package io.github.kmbisset89.worldweaver.domain

/**
 * Validates a Philips Hue bridge host (LAN IP or hostname).
 */
internal class HueBridgeHostParser {
    sealed interface Result {
        data class Valid(val host: String) : Result
        data object Blank : Result
        data object Invalid : Result
    }

    fun parse(raw: String): Result {
        val stripped = raw.trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .trimEnd('/')
            .substringBefore('/')
            .substringBefore(':')
        if (stripped.isEmpty()) {
            return Result.Blank
        }
        if (' ' in stripped || stripped.contains("..")) {
            return Result.Invalid
        }
        return Result.Valid(stripped)
    }
}

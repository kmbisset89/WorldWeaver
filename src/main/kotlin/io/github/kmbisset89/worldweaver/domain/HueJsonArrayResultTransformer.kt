package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * Reads Hue pairing and error payloads that often return HTTP 200 with a JSON array.
 */
internal class HueJsonArrayResultTransformer {
    sealed interface Result {
        data class Username(val username: String) : Result
        data object LinkButtonNotPressed : Result
        data object Unauthorized : Result
        data object Success : Result
        data class Failed(val message: String) : Result
    }

    fun transformPair(jsonText: String): Result {
        val first = firstObject(jsonText) ?: return Result.Failed("Hue returned an unexpected response")
        val success = first["success"] as? JsonObject
        val username = stringContent(success?.get("username"))
        if (!username.isNullOrBlank()) {
            return Result.Username(username)
        }
        return transformError(first)
    }

    fun transformAction(jsonText: String): Result {
        val first = firstObject(jsonText) ?: return Result.Failed("Hue returned an unexpected response")
        if (first["success"] != null) {
            return Result.Success
        }
        return transformError(first)
    }

    fun isErrorArray(jsonText: String): Boolean {
        return firstObject(jsonText)?.get("error") != null
    }

    private fun transformError(first: JsonObject): Result {
        val error = first["error"] as? JsonObject ?: return Result.Failed("Hue returned an error")
        val type = (error["type"] as? JsonPrimitive)?.intOrNull
        val description = stringContent(error["description"]).orEmpty()
        return when (type) {
            101 -> Result.LinkButtonNotPressed
            1 -> Result.Unauthorized
            else -> Result.Failed(description.ifBlank { "Hue returned an error" })
        }
    }

    private fun firstObject(jsonText: String): JsonObject? {
        val element = json.parseToJsonElement(jsonText)
        val array = try {
            element.jsonArray
        } catch (_: Exception) {
            return null
        }
        return array.firstOrNull() as? JsonObject
    }

    private fun stringContent(element: kotlinx.serialization.json.JsonElement?): String? {
        val primitive = element as? JsonPrimitive ?: return null
        return primitive.contentOrNull
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}

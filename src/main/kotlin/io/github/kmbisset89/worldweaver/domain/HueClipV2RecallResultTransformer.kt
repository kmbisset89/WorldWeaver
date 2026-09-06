package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * Reads a Hue CLIP v2 scene recall response.
 */
internal class HueClipV2RecallResultTransformer {
    sealed interface Result {
        data object Success : Result
        data object Unauthorized : Result
        data class Failed(val message: String) : Result
    }

    fun transform(statusCode: Int, jsonText: String): Result {
        val errors = errorDescriptions(jsonText)
        if (statusCode == 401 || statusCode == 403) {
            return Result.Unauthorized
        }
        if (errors.any { it.contains("unauthorized", ignoreCase = true) }) {
            return Result.Unauthorized
        }
        if (statusCode !in 200..299) {
            return Result.Failed(errors.firstOrNull() ?: "Hue returned HTTP $statusCode")
        }
        if (errors.isNotEmpty()) {
            return Result.Failed(errors.first())
        }
        return Result.Success
    }

    private fun errorDescriptions(jsonText: String): List<String> {
        val root = try {
            json.parseToJsonElement(jsonText).jsonObject
        } catch (_: Exception) {
            return emptyList()
        }
        val errors = try {
            root["errors"]?.jsonArray ?: return emptyList()
        } catch (_: Exception) {
            return emptyList()
        }
        return errors.mapNotNull { element ->
            val obj = element as? JsonObject ?: return@mapNotNull null
            val primitive = obj["description"] as? JsonPrimitive ?: return@mapNotNull null
            primitive.contentOrNull?.trim()?.takeIf { it.isNotEmpty() }
        }
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}

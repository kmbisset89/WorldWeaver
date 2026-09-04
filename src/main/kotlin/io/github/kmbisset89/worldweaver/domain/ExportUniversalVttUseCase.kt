package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.json.Json
import java.io.File

internal class ExportUniversalVttUseCase(
    private val battleMaps: BattleMapRepository,
    private val fileStore: BattleMapFileStore,
    private val documentFactory: UniversalVttDocumentFactory,
) {
    sealed interface Result {
        data object Written : Result
        data object MapNotFound : Result
        data object ImageMissing : Result
        data class Failed(val message: String) : Result
    }

    suspend operator fun invoke(battleMapId: String, destFile: File): Result {
        val battleMap = battleMaps.getById(battleMapId) ?: return Result.MapNotFound
        val originalPng = fileStore.readOriginalPng(battleMapId) ?: return Result.ImageMissing
        return try {
            val document = documentFactory.create(battleMap, originalPng)
            destFile.parentFile?.mkdirs()
            destFile.writeText(json.encodeToString(UniversalVttDocument.serializer(), document))
            Result.Written
        } catch (error: Exception) {
            Result.Failed(error.message ?: "Could not write the Universal VTT file")
        }
    }

    private companion object {
        val json = Json {
            prettyPrint = true
            encodeDefaults = true
        }
    }
}

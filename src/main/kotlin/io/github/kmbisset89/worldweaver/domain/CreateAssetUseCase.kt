package io.github.kmbisset89.worldweaver.domain

import java.io.File

internal class CreateAssetUseCase(
    private val assetRepository: AssetRepository,
    private val assetFileStore: AssetFileStore,
    private val activeContextRepository: ActiveContextRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data class Created(val asset: Asset) : Result
        data object MissingFile : Result
        data object EmptyFile : Result
        data object NoActiveWorld : Result
    }

    suspend operator fun invoke(sourceFile: File): Result {
        val worldId = activeContextRepository.get().activeWorldId ?: return Result.NoActiveWorld
        if (!sourceFile.isFile) {
            return Result.MissingFile
        }
        val bytes = sourceFile.readBytes()
        if (bytes.isEmpty()) {
            return Result.EmptyFile
        }
        val originalFileName = sourceFile.name.ifBlank { "file" }
        val now = instantProvider.now()
        val asset = Asset(
            id = entityIdFactory.create(),
            worldId = worldId,
            displayName = displayNameFrom(originalFileName),
            originalFileName = originalFileName,
            notes = "",
            byteSize = bytes.size.toLong(),
            createdAt = now,
            updatedAt = now,
        )
        assetFileStore.write(asset.id, originalFileName, bytes)
        assetRepository.insert(asset)
        return Result.Created(asset)
    }

    private fun displayNameFrom(originalFileName: String): String {
        val dot = originalFileName.lastIndexOf('.')
        val stem = if (dot > 0) {
            originalFileName.substring(0, dot)
        } else {
            originalFileName
        }
        return stem.ifBlank { originalFileName }
    }
}

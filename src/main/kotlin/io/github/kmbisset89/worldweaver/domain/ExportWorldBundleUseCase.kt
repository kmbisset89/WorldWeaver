package io.github.kmbisset89.worldweaver.domain

import java.io.File

internal class ExportWorldBundleUseCase(
    private val snapshotFactory: WorldBundleSnapshotFactory,
    private val archiveConverter: WorldBundleArchiveConverter,
) {
    sealed interface Result {
        data object Written : Result
        data object WorldNotFound : Result
        data class Failed(val message: String) : Result
    }

    suspend operator fun invoke(worldId: String, destFile: File): Result {
        val bundle = snapshotFactory.create(worldId) ?: return Result.WorldNotFound
        return try {
            archiveConverter.write(bundle, destFile)
            Result.Written
        } catch (error: Exception) {
            Result.Failed(error.message ?: "Could not write the world backup")
        }
    }
}

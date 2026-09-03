package io.github.kmbisset89.worldweaver.domain

import java.io.File

internal class ImportSrdCatalogUseCase(
    private val catalogRepository: SrdCatalogRepository,
    private val bundledLoader: BundledSrdCatalogLoader,
    private val converter: SrdCatalogJsonConverter,
    private val instantProvider: InstantProvider,
) {
    sealed interface Source {
        data object Bundled : Source
        data class File(val file: java.io.File) : Source
    }

    sealed interface Result {
        data class Imported(val catalog: SrdCatalog) : Result
        data object InvalidFile : Result
        data class Failed(val message: String) : Result
    }

    suspend operator fun invoke(source: Source): Result {
        val payload = when (source) {
            Source.Bundled -> {
                try {
                    bundledLoader.load()
                } catch (error: Exception) {
                    return Result.Failed(error.message ?: "Could not load the bundled SRD")
                }
            }
            is Source.File -> decodeFile(source.file) ?: return Result.InvalidFile
        }
        val catalog = converter.toCatalog(payload, instantProvider.now())
        if (!isValid(catalog)) {
            return Result.InvalidFile
        }
        return try {
            catalogRepository.write(catalog)
            Result.Imported(catalog)
        } catch (error: Exception) {
            Result.Failed(error.message ?: "Could not save the SRD catalog")
        }
    }

    private fun decodeFile(file: File): SrdCatalogPayload? {
        if (!file.isFile) {
            return null
        }
        return try {
            converter.decode(file.readText())
        } catch (_: Exception) {
            null
        }
    }

    private fun isValid(catalog: SrdCatalog): Boolean {
        if (catalog.formatVersion != SrdCatalog.FORMAT_VERSION) {
            return false
        }
        if (catalog.sourceLabel.isBlank()) {
            return false
        }
        return catalog.races.isNotEmpty() ||
            catalog.classes.isNotEmpty() ||
            catalog.spells.isNotEmpty() ||
            catalog.monsters.isNotEmpty()
    }
}

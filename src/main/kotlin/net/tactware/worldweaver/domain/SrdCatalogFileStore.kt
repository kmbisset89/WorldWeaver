package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.time.Instant

internal class SrdCatalogFileStore(
    private val srdDir: File,
    private val converter: SrdCatalogJsonConverter,
) : SrdCatalogRepository {
    private val catalogFile: File
        get() = File(srdDir, CATALOG_FILE_NAME)

    private val _catalog = MutableStateFlow(readOrNull())

    override fun observe(): Flow<SrdCatalog?> = _catalog.asStateFlow()

    override suspend fun get(): SrdCatalog? = _catalog.value

    override suspend fun write(catalog: SrdCatalog) {
        srdDir.mkdirs()
        catalogFile.writeText(converter.encode(converter.toPayload(catalog)))
        _catalog.value = catalog
    }

    override suspend fun clear() {
        if (catalogFile.isFile) {
            catalogFile.delete()
        }
        _catalog.value = null
    }

    private fun readOrNull(): SrdCatalog? {
        if (!catalogFile.isFile) {
            return null
        }
        return try {
            val payload = converter.decode(catalogFile.readText())
            val importedAt = if (payload.importedAtEpochMillis > 0) {
                Instant.ofEpochMilli(payload.importedAtEpochMillis)
            } else {
                Instant.EPOCH
            }
            converter.toCatalog(payload, importedAt)
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        const val CATALOG_FILE_NAME = "5e.json"
    }
}

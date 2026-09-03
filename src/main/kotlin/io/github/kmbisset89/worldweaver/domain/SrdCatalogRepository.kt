package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface SrdCatalogRepository {
    fun observe(): Flow<SrdCatalog?>

    suspend fun get(): SrdCatalog?

    suspend fun write(catalog: SrdCatalog)

    suspend fun clear()
}

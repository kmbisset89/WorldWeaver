package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class FakeSrdCatalogRepository : SrdCatalogRepository {
    private val catalog = MutableStateFlow<SrdCatalog?>(null)

    override fun observe(): Flow<SrdCatalog?> = catalog.asStateFlow()

    override suspend fun get(): SrdCatalog? = catalog.value

    override suspend fun write(catalog: SrdCatalog) {
        this.catalog.value = catalog
    }

    override suspend fun clear() {
        catalog.value = null
    }
}

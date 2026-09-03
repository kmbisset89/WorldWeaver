package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal class ObserveSrdCatalogUseCase(
    private val catalogRepository: SrdCatalogRepository,
) {
    operator fun invoke(): Flow<SrdCatalog?> {
        return catalogRepository.observe()
    }
}

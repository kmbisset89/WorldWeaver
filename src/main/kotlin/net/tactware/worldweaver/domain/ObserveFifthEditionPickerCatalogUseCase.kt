package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class ObserveFifthEditionPickerCatalogUseCase(
    private val catalogRepository: SrdCatalogRepository,
    private val resolver: FifthEditionPickerCatalogResolver,
) {
    operator fun invoke(): Flow<FifthEditionPickerCatalog> {
        return catalogRepository.observe().map { catalog ->
            resolver.resolve(catalog)
        }
    }
}

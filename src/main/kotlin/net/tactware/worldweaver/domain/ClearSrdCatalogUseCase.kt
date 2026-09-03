package net.tactware.worldweaver.domain

internal class ClearSrdCatalogUseCase(
    private val catalogRepository: SrdCatalogRepository,
) {
    suspend operator fun invoke() {
        catalogRepository.clear()
    }
}

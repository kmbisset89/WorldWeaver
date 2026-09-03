package io.github.kmbisset89.worldweaver.domain

internal class ClearSrdCatalogUseCase(
    private val catalogRepository: SrdCatalogRepository,
) {
    suspend operator fun invoke() {
        catalogRepository.clear()
    }
}

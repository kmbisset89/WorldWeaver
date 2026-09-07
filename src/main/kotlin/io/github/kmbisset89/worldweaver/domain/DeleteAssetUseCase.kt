package io.github.kmbisset89.worldweaver.domain

internal class DeleteAssetUseCase(
    private val assetRepository: AssetRepository,
    private val assetFileStore: AssetFileStore,
) {
    sealed interface Result {
        data object Deleted : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(assetId: String): Result {
        assetRepository.getById(assetId) ?: return Result.NotFound
        assetFileStore.delete(assetId)
        assetRepository.delete(assetId)
        return Result.Deleted
    }
}

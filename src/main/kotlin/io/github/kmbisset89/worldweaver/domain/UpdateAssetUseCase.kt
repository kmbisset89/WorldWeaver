package io.github.kmbisset89.worldweaver.domain

internal class UpdateAssetUseCase(
    private val assetRepository: AssetRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Updated : Result
        data object InvalidName : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(assetId: String, draft: AssetDraft): Result {
        val existing = assetRepository.getById(assetId) ?: return Result.NotFound
        val displayName = draft.displayName.trim()
        if (displayName.isEmpty()) {
            return Result.InvalidName
        }
        assetRepository.update(
            existing.copy(
                displayName = displayName,
                notes = draft.notes.trim(),
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Updated
    }
}

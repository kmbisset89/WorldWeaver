package net.tactware.worldweaver.domain

internal class UpdateCampaignUseCase(
    private val campaignRepository: CampaignRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Updated : Result
        data object InvalidName : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(
        campaignId: String,
        name: String,
        description: String,
        notes: String,
        gameSystem: GameSystem,
    ): Result {
        val existing = campaignRepository.getById(campaignId) ?: return Result.NotFound
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Result.InvalidName
        }
        campaignRepository.update(
            existing.copy(
                name = trimmedName,
                description = description.trim(),
                notes = notes.trim(),
                gameSystem = gameSystem,
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Updated
    }
}

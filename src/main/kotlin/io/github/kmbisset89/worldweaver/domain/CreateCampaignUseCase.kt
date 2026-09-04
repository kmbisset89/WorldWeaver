package io.github.kmbisset89.worldweaver.domain

internal class CreateCampaignUseCase(
    private val campaignRepository: CampaignRepository,
    private val activeContextRepository: ActiveContextRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
    private val setActiveCampaign: SetActiveCampaignUseCase,
) {
    sealed interface Result {
        data class Created(val campaign: Campaign) : Result
        data object InvalidName : Result
        data object NoActiveWorld : Result
    }

    suspend operator fun invoke(
        name: String,
        description: String,
        notes: String,
        gameSystem: GameSystem,
        levelingMode: LevelingMode = LevelingMode.Milestone,
    ): Result {
        val worldId = activeContextRepository.get().activeWorldId ?: return Result.NoActiveWorld
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Result.InvalidName
        }
        val now = instantProvider.now()
        val campaign = Campaign(
            id = entityIdFactory.create(),
            worldId = worldId,
            name = trimmedName,
            description = description.trim(),
            notes = notes.trim(),
            gameSystem = gameSystem,
            levelingMode = levelingMode,
            status = CampaignStatus.Active,
            createdAt = now,
            updatedAt = now,
        )
        campaignRepository.insert(campaign)
        setActiveCampaign(campaign.id)
        return Result.Created(campaign)
    }
}

package net.tactware.worldweaver.domain

internal class UpdateCampaignPersonDeathSavesUseCase(
    private val campaignPersonRepository: CampaignPersonRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Updated : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(
        personId: String,
        deathSaves: DeathSaves,
    ): Result {
        val person = campaignPersonRepository.getById(personId) ?: return Result.NotFound
        val sheet = person.sheet as? FifthEditionSheet ?: return Result.Updated
        val clamped = DeathSaves(
            successes = deathSaves.successes.coerceIn(0, DeathSaves.LIMIT),
            failures = deathSaves.failures.coerceIn(0, DeathSaves.LIMIT),
        )
        campaignPersonRepository.update(
            person.copy(
                sheet = sheet.copy(deathSaves = clamped),
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Updated
    }
}

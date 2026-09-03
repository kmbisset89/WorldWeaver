package io.github.kmbisset89.worldweaver.domain

internal class StartEncounterUseCase(
    private val encounterRepository: EncounterRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Started : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(encounterId: String): Result {
        val encounter = encounterRepository.getById(encounterId) ?: return Result.NotFound
        val now = instantProvider.now()
        encounterRepository.getByCampaign(encounter.campaignId)
            .filter { other -> other.status == EncounterStatus.Active && other.id != encounter.id }
            .forEach { other ->
                encounterRepository.update(
                    other.copy(
                        status = EncounterStatus.Planned,
                        updatedAt = now,
                    )
                )
            }
        encounterRepository.update(
            encounter.copy(
                status = EncounterStatus.Active,
                currentRound = 1,
                currentTurnIndex = 0,
                participants = encounter.participants.map { participant ->
                    participant.resetTurnEconomy()
                },
                updatedAt = now,
            )
        )
        return Result.Started
    }
}

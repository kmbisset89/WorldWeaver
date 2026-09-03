package io.github.kmbisset89.worldweaver.domain

internal class RollAllEncounterInitiativeUseCase(
    private val encounterRepository: EncounterRepository,
    private val instantProvider: InstantProvider,
    private val rollEncounterInitiative: RollEncounterInitiativeUseCase,
) {
    sealed interface Result {
        data object Rolled : Result
        data object NotFound : Result
        data object NoParticipants : Result
    }

    suspend operator fun invoke(
        encounterId: String,
        overwriteExisting: Boolean,
    ): Result {
        val encounter = encounterRepository.getById(encounterId) ?: return Result.NotFound
        if (encounter.participants.isEmpty()) {
            return Result.NoParticipants
        }
        val rolled = rollEncounterInitiative(encounter.participants, overwriteExisting)
        encounterRepository.update(
            encounter.copy(
                participants = rolled,
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Rolled
    }
}

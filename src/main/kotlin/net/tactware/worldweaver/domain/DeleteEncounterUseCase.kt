package net.tactware.worldweaver.domain

internal class DeleteEncounterUseCase(
    private val encounterRepository: EncounterRepository,
) {
    sealed interface Result {
        data object Deleted : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(encounterId: String): Result {
        encounterRepository.getById(encounterId) ?: return Result.NotFound
        encounterRepository.delete(encounterId)
        return Result.Deleted
    }
}

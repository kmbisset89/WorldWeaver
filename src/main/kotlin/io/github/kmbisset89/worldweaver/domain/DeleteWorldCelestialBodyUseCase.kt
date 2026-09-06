package io.github.kmbisset89.worldweaver.domain

internal class DeleteWorldCelestialBodyUseCase(
    private val bodyRepository: WorldCelestialBodyRepository,
) {
    sealed interface Result {
        data object Deleted : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(bodyId: String): Result {
        bodyRepository.getById(bodyId) ?: return Result.NotFound
        bodyRepository.delete(bodyId)
        return Result.Deleted
    }
}

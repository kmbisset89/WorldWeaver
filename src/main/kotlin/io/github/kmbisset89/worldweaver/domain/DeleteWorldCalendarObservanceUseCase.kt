package io.github.kmbisset89.worldweaver.domain

internal class DeleteWorldCalendarObservanceUseCase(
    private val observanceRepository: WorldCalendarObservanceRepository,
) {
    sealed interface Result {
        data object Deleted : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(observanceId: String): Result {
        observanceRepository.getById(observanceId) ?: return Result.NotFound
        observanceRepository.delete(observanceId)
        return Result.Deleted
    }
}

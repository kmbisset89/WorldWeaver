package io.github.kmbisset89.worldweaver.domain

internal class UpdateSessionClockUseCase(
    private val sessionClockRepository: SessionClockRepository,
) {
    sealed interface Result {
        data object Updated : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(clockId: String, filledCount: Int): Result {
        val existing = sessionClockRepository.getById(clockId) ?: return Result.NotFound
        val clamped = filledCount.coerceIn(0, existing.segmentCount)
        sessionClockRepository.update(existing.copy(filledCount = clamped))
        return Result.Updated
    }
}

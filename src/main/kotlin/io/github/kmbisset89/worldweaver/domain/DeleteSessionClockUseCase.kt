package io.github.kmbisset89.worldweaver.domain

internal class DeleteSessionClockUseCase(
    private val sessionClockRepository: SessionClockRepository,
) {
    sealed interface Result {
        data object Deleted : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(clockId: String): Result {
        sessionClockRepository.getById(clockId) ?: return Result.NotFound
        sessionClockRepository.delete(clockId)
        return Result.Deleted
    }
}

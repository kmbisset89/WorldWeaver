package io.github.kmbisset89.worldweaver.domain

internal class UpdateSessionRunnerNotesUseCase(
    private val sessionRepository: SessionRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Updated : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(
        sessionId: String,
        notes: String,
        scratchNotes: String,
    ): Result {
        val existing = sessionRepository.getById(sessionId) ?: return Result.NotFound
        sessionRepository.update(
            existing.copy(
                notes = notes.trim(),
                scratchNotes = scratchNotes.trim(),
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Updated
    }
}

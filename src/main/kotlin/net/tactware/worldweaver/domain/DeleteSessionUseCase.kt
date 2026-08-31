package net.tactware.worldweaver.domain

internal class DeleteSessionUseCase(
    private val sessionRepository: SessionRepository,
    private val questRepository: QuestRepository,
) {
    sealed interface Result {
        data object Deleted : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(sessionId: String): Result {
        sessionRepository.getById(sessionId) ?: return Result.NotFound
        questRepository.deleteLinksByTarget(QuestLinkKind.SESSION, sessionId)
        sessionRepository.delete(sessionId)
        return Result.Deleted
    }
}

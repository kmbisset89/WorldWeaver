package io.github.kmbisset89.worldweaver.domain

internal class UpdatePlotThreadUseCase(
    private val plotThreadRepository: PlotThreadRepository,
    private val sessionRepository: SessionRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Updated : Result
        data object InvalidTitle : Result
        data object InvalidSession : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(
        threadId: String,
        draft: PlotThreadDraft,
    ): Result {
        val existing = plotThreadRepository.getById(threadId) ?: return Result.NotFound
        val title = draft.title.trim()
        if (title.isEmpty()) {
            return Result.InvalidTitle
        }
        val sessionId = when (val resolved = resolveSession(draft.sessionId, existing.campaignId)) {
            SessionResolution.None -> null
            SessionResolution.Invalid -> return Result.InvalidSession
            is SessionResolution.Found -> resolved.id
        }
        plotThreadRepository.update(
            existing.copy(
                sessionId = sessionId,
                title = title,
                details = draft.details.trim(),
                status = draft.status,
                priority = draft.priority,
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Updated
    }

    private suspend fun resolveSession(
        sessionId: String?,
        campaignId: String,
    ): SessionResolution {
        val id = sessionId?.takeIf { it.isNotBlank() } ?: return SessionResolution.None
        val session = sessionRepository.getById(id) ?: return SessionResolution.Invalid
        return if (session.campaignId == campaignId) {
            SessionResolution.Found(id)
        } else {
            SessionResolution.Invalid
        }
    }

    private sealed interface SessionResolution {
        data object None : SessionResolution
        data object Invalid : SessionResolution
        data class Found(val id: String) : SessionResolution
    }
}

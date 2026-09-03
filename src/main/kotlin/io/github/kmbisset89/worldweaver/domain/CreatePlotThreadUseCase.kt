package io.github.kmbisset89.worldweaver.domain

internal class CreatePlotThreadUseCase(
    private val plotThreadRepository: PlotThreadRepository,
    private val sessionRepository: SessionRepository,
    private val activeContextRepository: ActiveContextRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data class Created(val thread: PlotThread) : Result
        data object InvalidTitle : Result
        data object InvalidSession : Result
        data object NoActiveCampaign : Result
    }

    suspend operator fun invoke(draft: PlotThreadDraft): Result {
        val campaignId = activeContextRepository.get().activeCampaignId
            ?: return Result.NoActiveCampaign
        val title = draft.title.trim()
        if (title.isEmpty()) {
            return Result.InvalidTitle
        }
        val sessionId = when (val resolved = resolveSession(draft.sessionId, campaignId)) {
            SessionResolution.None -> null
            SessionResolution.Invalid -> return Result.InvalidSession
            is SessionResolution.Found -> resolved.id
        }
        val now = instantProvider.now()
        val thread = PlotThread(
            id = entityIdFactory.create(),
            campaignId = campaignId,
            sessionId = sessionId,
            title = title,
            details = draft.details.trim(),
            status = draft.status,
            priority = draft.priority,
            createdAt = now,
            updatedAt = now,
        )
        plotThreadRepository.insert(thread)
        return Result.Created(thread)
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

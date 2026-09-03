package io.github.kmbisset89.worldweaver.domain

internal class CreateReferenceDocUseCase(
    private val referenceDocRepository: ReferenceDocRepository,
    private val sessionRepository: SessionRepository,
    private val activeContextRepository: ActiveContextRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data class Created(val doc: ReferenceDoc) : Result
        data object InvalidTitle : Result
        data object InvalidPath : Result
        data object InvalidSession : Result
        data object NoActiveCampaign : Result
    }

    suspend operator fun invoke(draft: ReferenceDocDraft): Result {
        val campaignId = activeContextRepository.get().activeCampaignId
            ?: return Result.NoActiveCampaign
        val title = draft.title.trim()
        if (title.isEmpty()) {
            return Result.InvalidTitle
        }
        val pathOrUrl = draft.pathOrUrl.trim()
        if (pathOrUrl.isEmpty()) {
            return Result.InvalidPath
        }
        val sessionId = when (val resolved = resolveSession(draft.sessionId, campaignId)) {
            SessionResolution.None -> null
            SessionResolution.Invalid -> return Result.InvalidSession
            is SessionResolution.Found -> resolved.id
        }
        val now = instantProvider.now()
        val doc = ReferenceDoc(
            id = entityIdFactory.create(),
            campaignId = campaignId,
            sessionId = sessionId,
            title = title,
            pathOrUrl = pathOrUrl,
            createdAt = now,
            updatedAt = now,
        )
        referenceDocRepository.insert(doc)
        return Result.Created(doc)
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

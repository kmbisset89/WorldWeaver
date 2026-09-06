package io.github.kmbisset89.worldweaver.domain

internal class DeleteCampaignUseCase(
    private val campaignRepository: CampaignRepository,
    private val sessionRepository: SessionRepository,
    private val sessionRecordingFileStore: SessionRecordingFileStore,
    private val activeContextRepository: ActiveContextRepository,
) {
    suspend operator fun invoke(campaignId: String) {
        val sessionIds = sessionRepository.getByCampaign(campaignId).map { session -> session.id }
        campaignRepository.delete(campaignId)
        sessionIds.forEach { sessionId ->
            sessionRecordingFileStore.deleteAll(sessionId)
        }
        if (activeContextRepository.get().activeCampaignId == campaignId) {
            activeContextRepository.setActiveCampaignId(null)
            activeContextRepository.setActiveSessionId(null)
        }
    }
}

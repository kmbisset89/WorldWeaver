package net.tactware.worldweaver.domain

internal class SetActiveSessionUseCase(
    private val sessionRepository: SessionRepository,
    private val campaignRepository: CampaignRepository,
    private val activeContextRepository: ActiveContextRepository,
) {
    suspend operator fun invoke(sessionId: String?) {
        if (sessionId == null) {
            activeContextRepository.setActiveSessionId(null)
            return
        }
        val session = sessionRepository.getById(sessionId) ?: return
        val campaign = campaignRepository.getById(session.campaignId) ?: return
        activeContextRepository.setActiveWorldId(campaign.worldId)
        activeContextRepository.setActiveCampaignId(campaign.id)
        activeContextRepository.setActiveSessionId(session.id)
    }
}

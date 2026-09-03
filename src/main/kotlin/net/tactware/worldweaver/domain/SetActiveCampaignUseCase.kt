package net.tactware.worldweaver.domain

internal class SetActiveCampaignUseCase(
    private val campaignRepository: CampaignRepository,
    private val activeContextRepository: ActiveContextRepository,
) {
    suspend operator fun invoke(campaignId: String) {
        val campaign = campaignRepository.getById(campaignId) ?: return
        val previousCampaignId = activeContextRepository.get().activeCampaignId
        activeContextRepository.setActiveWorldId(campaign.worldId)
        activeContextRepository.setActiveCampaignId(campaignId)
        if (previousCampaignId != campaignId) {
            activeContextRepository.setActiveSessionId(null)
        }
    }
}

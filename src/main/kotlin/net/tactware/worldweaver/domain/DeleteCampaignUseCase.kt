package net.tactware.worldweaver.domain

internal class DeleteCampaignUseCase(
    private val campaignRepository: CampaignRepository,
    private val activeContextRepository: ActiveContextRepository,
) {
    suspend operator fun invoke(campaignId: String) {
        campaignRepository.delete(campaignId)
        if (activeContextRepository.get().activeCampaignId == campaignId) {
            activeContextRepository.setActiveCampaignId(null)
        }
    }
}

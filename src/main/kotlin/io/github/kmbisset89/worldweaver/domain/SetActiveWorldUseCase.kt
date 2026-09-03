package io.github.kmbisset89.worldweaver.domain

internal class SetActiveWorldUseCase(
    private val worldRepository: WorldRepository,
    private val campaignRepository: CampaignRepository,
    private val activeContextRepository: ActiveContextRepository,
    private val instantProvider: InstantProvider,
) {
    suspend operator fun invoke(worldId: String) {
        val world = worldRepository.getById(worldId) ?: return
        val context = activeContextRepository.get()
        val campaignId = context.activeCampaignId
        if (campaignId != null) {
            val campaign = campaignRepository.getById(campaignId)
            if (campaign == null || campaign.worldId != worldId) {
                activeContextRepository.setActiveCampaignId(null)
                activeContextRepository.setActiveSessionId(null)
            }
        }
        activeContextRepository.setActiveWorldId(worldId)
        worldRepository.update(world.copy(updatedAt = instantProvider.now()))
    }
}

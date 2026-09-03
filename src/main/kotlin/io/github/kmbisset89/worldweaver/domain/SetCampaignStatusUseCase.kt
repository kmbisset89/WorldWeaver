package io.github.kmbisset89.worldweaver.domain

internal class SetCampaignStatusUseCase(
    private val campaignRepository: CampaignRepository,
    private val instantProvider: InstantProvider,
) {
    suspend operator fun invoke(
        campaignId: String,
        status: CampaignStatus,
    ) {
        val existing = campaignRepository.getById(campaignId) ?: return
        campaignRepository.update(
            existing.copy(
                status = status,
                updatedAt = instantProvider.now(),
            )
        )
    }
}

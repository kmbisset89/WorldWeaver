package net.tactware.worldweaver.domain

internal class ClearActiveCampaignUseCase(
    private val activeContextRepository: ActiveContextRepository,
) {
    operator fun invoke() {
        activeContextRepository.setActiveCampaignId(null)
    }
}

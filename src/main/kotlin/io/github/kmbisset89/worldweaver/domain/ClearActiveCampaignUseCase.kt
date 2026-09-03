package io.github.kmbisset89.worldweaver.domain

internal class ClearActiveCampaignUseCase(
    private val activeContextRepository: ActiveContextRepository,
) {
    operator fun invoke() {
        activeContextRepository.setActiveCampaignId(null)
        activeContextRepository.setActiveSessionId(null)
    }
}

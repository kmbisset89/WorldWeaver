package io.github.kmbisset89.worldweaver.domain

internal class FindSessionCalendarMonthIdsForWorldUseCase(
    private val campaignRepository: CampaignRepository,
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(worldId: String): Set<String> {
        return campaignRepository.getByWorld(worldId)
            .flatMap { campaign -> sessionRepository.getByCampaign(campaign.id) }
            .mapNotNull { session -> session.inWorldDate?.monthId }
            .toSet()
    }
}

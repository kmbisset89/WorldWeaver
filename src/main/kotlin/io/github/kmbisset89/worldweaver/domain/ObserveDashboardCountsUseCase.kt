package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

internal class ObserveDashboardCountsUseCase(
    private val worldRepository: WorldRepository,
    private val campaignRepository: CampaignRepository,
    private val worldPersonRepository: WorldPersonRepository,
    private val campaignPersonRepository: CampaignPersonRepository,
) {
    operator fun invoke(): Flow<DashboardCounts> {
        return combine(
            worldRepository.observeCount(),
            campaignRepository.observeCount(),
            worldPersonRepository.observeCount(),
            campaignPersonRepository.observeCount(),
        ) { worlds, campaigns, worldPeople, campaignPeople ->
            DashboardCounts(
                worlds = worlds,
                campaigns = campaigns,
                people = worldPeople + campaignPeople,
            )
        }
    }
}

package net.tactware.worldweaver.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

internal class ObservePeopleForActiveContextUseCase(
    private val worldPersonRepository: WorldPersonRepository,
    private val campaignPersonRepository: CampaignPersonRepository,
    private val activeContextRepository: ActiveContextRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<PeopleSnapshot> {
        return activeContextRepository.observe().flatMapLatest { context ->
            val worldId = context.activeWorldId
            val campaignId = context.activeCampaignId
            val worldFlow = if (worldId == null) {
                flowOf(emptyList())
            } else {
                worldPersonRepository.observeByWorld(worldId)
            }
            val campaignFlow = if (campaignId == null) {
                flowOf(emptyList())
            } else {
                campaignPersonRepository.observeByCampaign(campaignId)
            }
            combine(worldFlow, campaignFlow) { worldPeople, campaignPeople ->
                PeopleSnapshot(
                    worldPeople = worldPeople,
                    campaignPeople = campaignPeople,
                )
            }
        }
    }
}

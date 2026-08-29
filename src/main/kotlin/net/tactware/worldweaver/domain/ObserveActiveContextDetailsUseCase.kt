package net.tactware.worldweaver.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

internal class ObserveActiveContextDetailsUseCase(
    private val activeContextRepository: ActiveContextRepository,
    private val worldRepository: WorldRepository,
    private val campaignRepository: CampaignRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<ActiveContextDetails> {
        return activeContextRepository.observe().flatMapLatest { context ->
            val worldFlow = context.activeWorldId?.let { worldId ->
                worldRepository.observeById(worldId)
            } ?: flowOf(null)
            val campaignFlow = context.activeCampaignId?.let { campaignId ->
                campaignRepository.observeById(campaignId)
            } ?: flowOf(null)
            combine(worldFlow, campaignFlow) { world, campaign ->
                val resolvedCampaign = if (
                    campaign != null &&
                    world != null &&
                    campaign.worldId == world.id
                ) {
                    campaign
                } else {
                    null
                }
                ActiveContextDetails(
                    world = world,
                    campaign = resolvedCampaign,
                )
            }
        }
    }
}

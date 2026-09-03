package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

internal class ObserveQuestsForActiveCampaignUseCase(
    private val questRepository: QuestRepository,
    private val activeContextRepository: ActiveContextRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<Quest>> {
        return activeContextRepository.observe().flatMapLatest { context ->
            val campaignId = context.activeCampaignId
            if (campaignId == null) {
                flowOf(emptyList())
            } else {
                questRepository.observeByCampaign(campaignId)
            }
        }
    }
}

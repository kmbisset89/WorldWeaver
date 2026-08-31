package net.tactware.worldweaver.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

internal class ObserveReferenceDocsForActiveCampaignUseCase(
    private val referenceDocRepository: ReferenceDocRepository,
    private val activeContextRepository: ActiveContextRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<ReferenceDoc>> {
        return activeContextRepository.observe().flatMapLatest { context ->
            val campaignId = context.activeCampaignId
            if (campaignId == null) {
                flowOf(emptyList())
            } else {
                referenceDocRepository.observeByCampaign(campaignId)
            }
        }
    }
}

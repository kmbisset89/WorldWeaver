package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class FakeActiveContextRepository : ActiveContextRepository {
    private val state = MutableStateFlow(
        ActiveContext(
            activeWorldId = null,
            activeCampaignId = null,
        )
    )

    override fun observe(): Flow<ActiveContext> = state.asStateFlow()

    override fun get(): ActiveContext = state.value

    override fun setActiveWorldId(worldId: String?) {
        state.value = state.value.copy(activeWorldId = worldId)
    }

    override fun setActiveCampaignId(campaignId: String?) {
        state.value = state.value.copy(activeCampaignId = campaignId)
    }

    override fun setActiveSessionId(sessionId: String?) {
        state.value = state.value.copy(activeSessionId = sessionId)
    }
}

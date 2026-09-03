package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface ActiveContextRepository {
    fun observe(): Flow<ActiveContext>
    fun get(): ActiveContext
    fun setActiveWorldId(worldId: String?)
    fun setActiveCampaignId(campaignId: String?)
    fun setActiveSessionId(sessionId: String?)
}

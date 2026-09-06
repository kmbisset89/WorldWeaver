package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface SessionClockRepository {
    fun observeBySession(sessionId: String): Flow<List<SessionClock>>
    suspend fun getById(id: String): SessionClock?
    suspend fun getBySession(sessionId: String): List<SessionClock>
    suspend fun getByCampaign(campaignId: String): List<SessionClock>
    suspend fun insert(clock: SessionClock)
    suspend fun update(clock: SessionClock)
    suspend fun delete(id: String)
}

package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface SessionRepository {
    fun observeByCampaign(campaignId: String): Flow<List<Session>>
    suspend fun getById(id: String): Session?
    suspend fun getByCampaign(campaignId: String): List<Session>
    suspend fun search(query: String): List<Session>
    suspend fun insert(session: Session)
    suspend fun update(session: Session)
    suspend fun delete(id: String)
}

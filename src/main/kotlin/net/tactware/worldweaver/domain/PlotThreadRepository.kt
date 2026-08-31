package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface PlotThreadRepository {
    fun observeByCampaign(campaignId: String): Flow<List<PlotThread>>
    suspend fun getById(id: String): PlotThread?
    suspend fun getByCampaign(campaignId: String): List<PlotThread>
    suspend fun insert(thread: PlotThread)
    suspend fun update(thread: PlotThread)
    suspend fun delete(id: String)
}

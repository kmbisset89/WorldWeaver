package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface CampaignRepository {
    fun observeByWorld(worldId: String): Flow<List<Campaign>>
    fun observeById(id: String): Flow<Campaign?>
    suspend fun getById(id: String): Campaign?
    suspend fun countByWorld(worldId: String): Int
    suspend fun insert(campaign: Campaign)
    suspend fun update(campaign: Campaign)
    suspend fun delete(id: String)
}

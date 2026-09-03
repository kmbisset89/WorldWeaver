package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface CampaignRepository {
    fun observeByWorld(worldId: String): Flow<List<Campaign>>
    fun observeById(id: String): Flow<Campaign?>
    suspend fun getById(id: String): Campaign?
    suspend fun getByWorld(worldId: String): List<Campaign>
    suspend fun search(query: String): List<Campaign>
    suspend fun countByWorld(worldId: String): Int
    fun observeCount(): Flow<Int>
    suspend fun insert(campaign: Campaign)
    suspend fun update(campaign: Campaign)
    suspend fun delete(id: String)
}

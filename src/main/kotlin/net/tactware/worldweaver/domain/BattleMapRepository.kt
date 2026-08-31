package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface BattleMapRepository {
    fun observeByCampaign(campaignId: String): Flow<List<BattleMap>>
    suspend fun getById(id: String): BattleMap?
    suspend fun getByCampaign(campaignId: String): List<BattleMap>
    suspend fun insert(battleMap: BattleMap)
    suspend fun update(battleMap: BattleMap)
    suspend fun delete(id: String)
}

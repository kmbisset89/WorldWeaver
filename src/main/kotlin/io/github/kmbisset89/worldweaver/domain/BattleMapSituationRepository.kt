package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface BattleMapSituationRepository {
    fun observeByCampaign(campaignId: String): Flow<List<BattleMapSituation>>
    suspend fun getById(id: String): BattleMapSituation?
    suspend fun getByBattleMap(battleMapId: String): List<BattleMapSituation>
    suspend fun insert(situation: BattleMapSituation)
    suspend fun update(situation: BattleMapSituation)
    suspend fun delete(id: String)
}

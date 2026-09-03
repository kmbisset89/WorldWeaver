package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeBattleMapRepository : BattleMapRepository {
    private val maps = MutableStateFlow<List<BattleMap>>(emptyList())

    fun all(): List<BattleMap> = maps.value

    override fun observeByCampaign(campaignId: String): Flow<List<BattleMap>> {
        return maps.map { list -> list.filter { it.campaignId == campaignId } }
    }

    override suspend fun getById(id: String): BattleMap? {
        return maps.value.firstOrNull { it.id == id }
    }

    override suspend fun getByCampaign(campaignId: String): List<BattleMap> {
        return maps.value.filter { it.campaignId == campaignId }
    }

    override suspend fun insert(battleMap: BattleMap) {
        maps.value = maps.value + battleMap
    }

    override suspend fun update(battleMap: BattleMap) {
        maps.value = maps.value.map { current ->
            if (current.id == battleMap.id) battleMap else current
        }
    }

    override suspend fun delete(id: String) {
        maps.value = maps.value.filterNot { it.id == id }
    }
}

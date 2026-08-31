package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class FakeBattleMapSituationRepository : BattleMapSituationRepository {
    private val situations = MutableStateFlow<List<BattleMapSituation>>(emptyList())

    fun all(): List<BattleMapSituation> = situations.value

    override fun observeByCampaign(campaignId: String): Flow<List<BattleMapSituation>> {
        return situations.asStateFlow()
    }

    override suspend fun getById(id: String): BattleMapSituation? {
        return situations.value.firstOrNull { it.id == id }
    }

    override suspend fun getByBattleMap(battleMapId: String): List<BattleMapSituation> {
        return situations.value.filter { it.battleMapId == battleMapId }
    }

    override suspend fun insert(situation: BattleMapSituation) {
        situations.value = situations.value + situation
    }

    override suspend fun update(situation: BattleMapSituation) {
        situations.value = situations.value.map { current ->
            if (current.id == situation.id) situation else current
        }
    }

    override suspend fun delete(id: String) {
        situations.value = situations.value.filterNot { it.id == id }
    }
}

package io.github.kmbisset89.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import io.github.kmbisset89.worldweaver.domain.BattleMap
import io.github.kmbisset89.worldweaver.domain.BattleMapRepository

internal class BattleMapRepositoryImpl(
    private val battleMapDao: BattleMapDao,
    private val converter: BattleMapEntityConverter,
) : BattleMapRepository {
    override fun observeByCampaign(campaignId: String): Flow<List<BattleMap>> {
        return battleMapDao.observeByCampaign(campaignId).map { entities ->
            entities.map { converter.toBattleMap(it) }
        }
    }

    override suspend fun getById(id: String): BattleMap? {
        return battleMapDao.getById(id)?.let { converter.toBattleMap(it) }
    }

    override suspend fun getByCampaign(campaignId: String): List<BattleMap> {
        return battleMapDao.getByCampaign(campaignId).map { converter.toBattleMap(it) }
    }

    override suspend fun insert(battleMap: BattleMap) {
        battleMapDao.insert(converter.toEntity(battleMap))
    }

    override suspend fun update(battleMap: BattleMap) {
        battleMapDao.update(converter.toEntity(battleMap))
    }

    override suspend fun delete(id: String) {
        battleMapDao.delete(id)
    }
}

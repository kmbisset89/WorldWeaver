package io.github.kmbisset89.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import io.github.kmbisset89.worldweaver.domain.BattleMapSituation
import io.github.kmbisset89.worldweaver.domain.BattleMapSituationRepository

internal class BattleMapSituationRepositoryImpl(
    private val situationDao: BattleMapSituationDao,
    private val converter: BattleMapSituationEntityConverter,
) : BattleMapSituationRepository {
    override fun observeByCampaign(campaignId: String): Flow<List<BattleMapSituation>> {
        return situationDao.observeByCampaign(campaignId).map { entities ->
            entities.map { converter.toSituation(it) }
        }
    }

    override suspend fun getById(id: String): BattleMapSituation? {
        return situationDao.getById(id)?.let { converter.toSituation(it) }
    }

    override suspend fun getByBattleMap(battleMapId: String): List<BattleMapSituation> {
        return situationDao.getByBattleMap(battleMapId).map { converter.toSituation(it) }
    }

    override suspend fun insert(situation: BattleMapSituation) {
        situationDao.insert(converter.toEntity(situation))
    }

    override suspend fun update(situation: BattleMapSituation) {
        situationDao.update(converter.toEntity(situation))
    }

    override suspend fun delete(id: String) {
        situationDao.delete(id)
    }
}

package io.github.kmbisset89.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import io.github.kmbisset89.worldweaver.domain.WorldCalendarObservance
import io.github.kmbisset89.worldweaver.domain.WorldCalendarObservanceRepository

internal class WorldCalendarObservanceRepositoryImpl(
    private val observanceDao: WorldCalendarObservanceDao,
    private val loreLinkDao: WorldCalendarObservanceLoreLinkDao,
    private val converter: WorldCalendarObservanceEntityConverter,
) : WorldCalendarObservanceRepository {
    override fun observeByWorld(worldId: String): Flow<List<WorldCalendarObservance>> {
        return combine(
            observanceDao.observeByWorld(worldId),
            loreLinkDao.observeByWorld(worldId),
        ) { observances, links ->
            assemble(observances, links)
        }
    }

    override suspend fun getById(id: String): WorldCalendarObservance? {
        val entity = observanceDao.getById(id) ?: return null
        val loreIds = loreLinkDao.getByObservance(id).map { it.loreId }
        return converter.toObservance(entity, loreIds)
    }

    override suspend fun getByWorld(worldId: String): List<WorldCalendarObservance> {
        return assemble(
            observanceDao.getByWorld(worldId),
            loreLinkDao.getByWorld(worldId),
        )
    }

    override suspend fun search(query: String): List<WorldCalendarObservance> {
        return observanceDao.searchLike(query).map { entity ->
            val loreIds = loreLinkDao.getByObservance(entity.id).map { it.loreId }
            converter.toObservance(entity, loreIds)
        }
    }

    override suspend fun insert(observance: WorldCalendarObservance) {
        observanceDao.insert(converter.toEntity(observance))
        replaceLinks(observance)
    }

    override suspend fun update(observance: WorldCalendarObservance) {
        observanceDao.update(converter.toEntity(observance))
        replaceLinks(observance)
    }

    override suspend fun delete(id: String) {
        observanceDao.delete(id)
    }

    private suspend fun replaceLinks(observance: WorldCalendarObservance) {
        loreLinkDao.deleteByObservance(observance.id)
        val links = converter.toLinkEntities(observance)
        if (links.isNotEmpty()) {
            loreLinkDao.insertAll(links)
        }
    }

    private fun assemble(
        observances: List<WorldCalendarObservanceEntity>,
        links: List<WorldCalendarObservanceLoreLinkEntity>,
    ): List<WorldCalendarObservance> {
        val loreIdsByObservance = links.groupBy { it.observanceId }
        return observances.map { entity ->
            converter.toObservance(
                entity,
                loreIdsByObservance[entity.id].orEmpty().map { it.loreId },
            )
        }
    }
}

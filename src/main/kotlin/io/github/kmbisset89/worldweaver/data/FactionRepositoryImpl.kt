package io.github.kmbisset89.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import io.github.kmbisset89.worldweaver.domain.Faction
import io.github.kmbisset89.worldweaver.domain.FactionRepository

internal class FactionRepositoryImpl(
    private val factionDao: FactionDao,
    private val converter: FactionEntityConverter,
) : FactionRepository {
    override fun observeByWorld(worldId: String): Flow<List<Faction>> {
        return factionDao.observeByWorld(worldId).map { entities ->
            entities.map { converter.toFaction(it) }
        }
    }

    override suspend fun getById(id: String): Faction? {
        return factionDao.getById(id)?.let { converter.toFaction(it) }
    }

    override suspend fun getByWorld(worldId: String): List<Faction> {
        return factionDao.getByWorld(worldId).map { converter.toFaction(it) }
    }

    override suspend fun search(query: String): List<Faction> {
        return factionDao.searchLike(query).map { converter.toFaction(it) }
    }

    override suspend fun insert(faction: Faction) {
        factionDao.insert(converter.toEntity(faction))
    }

    override suspend fun update(faction: Faction) {
        factionDao.update(converter.toEntity(faction))
    }

    override suspend fun delete(id: String) {
        factionDao.delete(id)
    }
}

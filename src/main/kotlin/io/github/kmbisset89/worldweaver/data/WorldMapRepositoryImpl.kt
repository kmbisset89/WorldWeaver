package io.github.kmbisset89.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import io.github.kmbisset89.worldweaver.domain.WorldMap
import io.github.kmbisset89.worldweaver.domain.WorldMapRepository

internal class WorldMapRepositoryImpl(
    private val worldMapDao: WorldMapDao,
    private val converter: WorldMapEntityConverter,
) : WorldMapRepository {
    override fun observeByWorld(worldId: String): Flow<List<WorldMap>> {
        return worldMapDao.observeByWorld(worldId).map { entities ->
            entities.map { converter.toWorldMap(it) }
        }
    }

    override suspend fun getById(id: String): WorldMap? {
        return worldMapDao.getById(id)?.let { converter.toWorldMap(it) }
    }

    override suspend fun getByWorld(worldId: String): List<WorldMap> {
        return worldMapDao.getByWorld(worldId).map { converter.toWorldMap(it) }
    }

    override suspend fun getByLocation(worldId: String, locationId: String?): WorldMap? {
        val entity = if (locationId == null) {
            worldMapDao.getWorldRoot(worldId)
        } else {
            worldMapDao.getByLocationId(locationId)
        }
        return entity?.let { converter.toWorldMap(it) }
    }

    override suspend fun insert(worldMap: WorldMap) {
        worldMapDao.insert(converter.toEntity(worldMap))
    }

    override suspend fun update(worldMap: WorldMap) {
        worldMapDao.update(converter.toEntity(worldMap))
    }

    override suspend fun delete(id: String) {
        worldMapDao.delete(id)
    }
}

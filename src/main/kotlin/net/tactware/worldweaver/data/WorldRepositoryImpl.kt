package net.tactware.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.tactware.worldweaver.domain.World
import net.tactware.worldweaver.domain.WorldRepository

internal class WorldRepositoryImpl(
    private val worldDao: WorldDao,
    private val converter: WorldEntityConverter,
) : WorldRepository {
    override fun observeAll(): Flow<List<World>> {
        return worldDao.observeAll().map { entities ->
            entities.map { converter.toWorld(it) }
        }
    }

    override fun observeCount(): Flow<Int> {
        return worldDao.observeCount()
    }

    override fun observeById(id: String): Flow<World?> {
        return worldDao.observeById(id).map { entity ->
            entity?.let { converter.toWorld(it) }
        }
    }

    override suspend fun getById(id: String): World? {
        return worldDao.getById(id)?.let { converter.toWorld(it) }
    }

    override suspend fun search(query: String): List<World> {
        return worldDao.searchLike(query).map { converter.toWorld(it) }
    }

    override suspend fun insert(world: World) {
        worldDao.insert(converter.toEntity(world))
    }

    override suspend fun update(world: World) {
        worldDao.update(converter.toEntity(world))
    }

    override suspend fun delete(id: String) {
        worldDao.delete(id)
    }
}

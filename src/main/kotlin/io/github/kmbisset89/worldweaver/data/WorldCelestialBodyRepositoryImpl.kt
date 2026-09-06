package io.github.kmbisset89.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import io.github.kmbisset89.worldweaver.domain.WorldCelestialBody
import io.github.kmbisset89.worldweaver.domain.WorldCelestialBodyRepository

internal class WorldCelestialBodyRepositoryImpl(
    private val bodyDao: WorldCelestialBodyDao,
    private val converter: WorldCelestialBodyEntityConverter,
) : WorldCelestialBodyRepository {
    override fun observeByWorld(worldId: String): Flow<List<WorldCelestialBody>> {
        return bodyDao.observeByWorld(worldId).map { entities ->
            entities.map(converter::toBody)
        }
    }

    override suspend fun getById(id: String): WorldCelestialBody? {
        return bodyDao.getById(id)?.let(converter::toBody)
    }

    override suspend fun getByWorld(worldId: String): List<WorldCelestialBody> {
        return bodyDao.getByWorld(worldId).map(converter::toBody)
    }

    override suspend fun search(query: String): List<WorldCelestialBody> {
        return bodyDao.searchLike(query).map(converter::toBody)
    }

    override suspend fun insert(body: WorldCelestialBody) {
        bodyDao.insert(converter.toEntity(body))
    }

    override suspend fun update(body: WorldCelestialBody) {
        bodyDao.update(converter.toEntity(body))
    }

    override suspend fun delete(id: String) {
        bodyDao.delete(id)
    }
}

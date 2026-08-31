package net.tactware.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.tactware.worldweaver.domain.Location
import net.tactware.worldweaver.domain.LocationRepository

internal class LocationRepositoryImpl(
    private val locationDao: LocationDao,
    private val converter: LocationEntityConverter,
) : LocationRepository {
    override fun observeByWorld(worldId: String): Flow<List<Location>> {
        return locationDao.observeByWorld(worldId).map { entities ->
            entities.map { converter.toLocation(it) }
        }
    }

    override fun observeById(id: String): Flow<Location?> {
        return locationDao.observeById(id).map { entity ->
            entity?.let { converter.toLocation(it) }
        }
    }

    override suspend fun getById(id: String): Location? {
        return locationDao.getById(id)?.let { converter.toLocation(it) }
    }

    override suspend fun getByWorld(worldId: String): List<Location> {
        return locationDao.getByWorld(worldId).map { converter.toLocation(it) }
    }

    override suspend fun search(query: String): List<Location> {
        return locationDao.searchLike(query).map { converter.toLocation(it) }
    }

    override suspend fun countByParent(parentLocationId: String): Int {
        return locationDao.countByParent(parentLocationId)
    }

    override suspend fun insert(location: Location) {
        locationDao.insert(converter.toEntity(location))
    }

    override suspend fun update(location: Location) {
        locationDao.update(converter.toEntity(location))
    }

    override suspend fun delete(id: String) {
        locationDao.delete(id)
    }
}

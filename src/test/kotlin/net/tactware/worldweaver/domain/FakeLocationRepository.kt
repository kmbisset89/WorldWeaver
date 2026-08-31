package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeLocationRepository : LocationRepository {
    private val locations = MutableStateFlow<List<Location>>(emptyList())

    fun all(): List<Location> = locations.value

    override fun observeByWorld(worldId: String): Flow<List<Location>> {
        return locations.map { list -> list.filter { it.worldId == worldId } }
    }

    override fun observeById(id: String): Flow<Location?> {
        return locations.map { list -> list.firstOrNull { it.id == id } }
    }

    override suspend fun getById(id: String): Location? {
        return locations.value.firstOrNull { it.id == id }
    }

    override suspend fun getByWorld(worldId: String): List<Location> {
        return locations.value.filter { it.worldId == worldId }
    }

    override suspend fun search(query: String): List<Location> {
        return locations.value.filter { location ->
            location.name.contains(query, ignoreCase = true) ||
                location.description.contains(query, ignoreCase = true) ||
                location.climate.contains(query, ignoreCase = true) ||
                location.terrain.contains(query, ignoreCase = true) ||
                location.government.contains(query, ignoreCase = true) ||
                location.history.contains(query, ignoreCase = true) ||
                location.notes.contains(query, ignoreCase = true) ||
                location.landmarks.any { it.contains(query, ignoreCase = true) }
        }
    }

    override suspend fun countByParent(parentLocationId: String): Int {
        return locations.value.count { it.parentLocationId == parentLocationId }
    }

    override suspend fun insert(location: Location) {
        locations.value = locations.value + location
    }

    override suspend fun update(location: Location) {
        locations.value = locations.value.map { current ->
            if (current.id == location.id) location else current
        }
    }

    override suspend fun delete(id: String) {
        locations.value = locations.value.filterNot { it.id == id }
    }
}

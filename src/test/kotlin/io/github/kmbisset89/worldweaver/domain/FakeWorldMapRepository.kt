package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeWorldMapRepository : WorldMapRepository {
    private val maps = MutableStateFlow<List<WorldMap>>(emptyList())

    fun all(): List<WorldMap> = maps.value

    override fun observeByWorld(worldId: String): Flow<List<WorldMap>> {
        return maps.map { list -> list.filter { it.worldId == worldId } }
    }

    override suspend fun getById(id: String): WorldMap? {
        return maps.value.firstOrNull { it.id == id }
    }

    override suspend fun getByWorld(worldId: String): List<WorldMap> {
        return maps.value.filter { it.worldId == worldId }
    }

    override suspend fun getByLocation(worldId: String, locationId: String?): WorldMap? {
        return maps.value.firstOrNull { map ->
            map.worldId == worldId && map.locationId == locationId
        }
    }

    override suspend fun insert(worldMap: WorldMap) {
        maps.value = maps.value + worldMap
    }

    override suspend fun update(worldMap: WorldMap) {
        maps.value = maps.value.map { current ->
            if (current.id == worldMap.id) worldMap else current
        }
    }

    override suspend fun delete(id: String) {
        maps.value = maps.value.filterNot { it.id == id }
    }
}

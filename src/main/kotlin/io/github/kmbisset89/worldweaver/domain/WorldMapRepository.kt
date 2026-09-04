package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface WorldMapRepository {
    fun observeByWorld(worldId: String): Flow<List<WorldMap>>
    suspend fun getById(id: String): WorldMap?
    suspend fun getByWorld(worldId: String): List<WorldMap>
    suspend fun getByLocation(worldId: String, locationId: String?): WorldMap?
    suspend fun insert(worldMap: WorldMap)
    suspend fun update(worldMap: WorldMap)
    suspend fun delete(id: String)
}

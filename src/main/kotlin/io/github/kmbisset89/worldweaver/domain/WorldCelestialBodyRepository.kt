package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface WorldCelestialBodyRepository {
    fun observeByWorld(worldId: String): Flow<List<WorldCelestialBody>>
    suspend fun getById(id: String): WorldCelestialBody?
    suspend fun getByWorld(worldId: String): List<WorldCelestialBody>
    suspend fun search(query: String): List<WorldCelestialBody>
    suspend fun insert(body: WorldCelestialBody)
    suspend fun update(body: WorldCelestialBody)
    suspend fun delete(id: String)
}

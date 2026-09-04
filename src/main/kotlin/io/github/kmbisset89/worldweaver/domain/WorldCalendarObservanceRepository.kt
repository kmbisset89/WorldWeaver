package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface WorldCalendarObservanceRepository {
    fun observeByWorld(worldId: String): Flow<List<WorldCalendarObservance>>
    suspend fun getById(id: String): WorldCalendarObservance?
    suspend fun getByWorld(worldId: String): List<WorldCalendarObservance>
    suspend fun search(query: String): List<WorldCalendarObservance>
    suspend fun insert(observance: WorldCalendarObservance)
    suspend fun update(observance: WorldCalendarObservance)
    suspend fun delete(id: String)
}

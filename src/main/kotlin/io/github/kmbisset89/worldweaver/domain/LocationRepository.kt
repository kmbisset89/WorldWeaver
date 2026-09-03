package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface LocationRepository {
    fun observeByWorld(worldId: String): Flow<List<Location>>
    fun observeById(id: String): Flow<Location?>
    suspend fun getById(id: String): Location?
    suspend fun getByWorld(worldId: String): List<Location>
    suspend fun search(query: String): List<Location>
    suspend fun countByParent(parentLocationId: String): Int
    suspend fun insert(location: Location)
    suspend fun update(location: Location)
    suspend fun delete(id: String)
}

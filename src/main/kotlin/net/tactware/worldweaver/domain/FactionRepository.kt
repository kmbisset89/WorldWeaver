package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface FactionRepository {
    fun observeByWorld(worldId: String): Flow<List<Faction>>
    suspend fun getById(id: String): Faction?
    suspend fun getByWorld(worldId: String): List<Faction>
    suspend fun search(query: String): List<Faction>
    suspend fun insert(faction: Faction)
    suspend fun update(faction: Faction)
    suspend fun delete(id: String)
}

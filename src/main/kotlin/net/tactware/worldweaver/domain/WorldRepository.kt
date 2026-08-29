package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface WorldRepository {
    fun observeAll(): Flow<List<World>>
    fun observeById(id: String): Flow<World?>
    suspend fun getById(id: String): World?
    suspend fun insert(world: World)
    suspend fun update(world: World)
    suspend fun delete(id: String)
}

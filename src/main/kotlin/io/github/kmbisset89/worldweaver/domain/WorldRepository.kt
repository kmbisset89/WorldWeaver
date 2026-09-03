package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface WorldRepository {
    fun observeAll(): Flow<List<World>>
    fun observeCount(): Flow<Int>
    fun observeById(id: String): Flow<World?>
    suspend fun getById(id: String): World?
    suspend fun search(query: String): List<World>
    suspend fun insert(world: World)
    suspend fun update(world: World)
    suspend fun delete(id: String)
}

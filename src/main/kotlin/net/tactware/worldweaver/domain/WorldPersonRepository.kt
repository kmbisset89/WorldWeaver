package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface WorldPersonRepository {
    fun observeByWorld(worldId: String): Flow<List<WorldPerson>>
    fun observeCount(): Flow<Int>
    suspend fun getById(id: String): WorldPerson?
    suspend fun getByWorld(worldId: String): List<WorldPerson>
    suspend fun search(query: String): List<WorldPerson>
    suspend fun insert(person: WorldPerson)
    suspend fun update(person: WorldPerson)
    suspend fun delete(id: String)
}

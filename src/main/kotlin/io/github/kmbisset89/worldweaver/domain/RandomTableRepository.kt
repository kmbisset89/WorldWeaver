package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface RandomTableRepository {
    fun observeByWorld(worldId: String): Flow<List<RandomTable>>
    suspend fun getById(id: String): RandomTable?
    suspend fun getByWorld(worldId: String): List<RandomTable>
    suspend fun search(query: String): List<RandomTable>
    suspend fun insert(table: RandomTable)
    suspend fun update(table: RandomTable)
    suspend fun delete(id: String)
}

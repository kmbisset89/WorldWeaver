package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface LoreRepository {
    fun observeByWorld(worldId: String): Flow<List<Lore>>
    suspend fun getById(id: String): Lore?
    suspend fun getByWorld(worldId: String): List<Lore>
    suspend fun search(query: String): List<Lore>
    suspend fun insert(lore: Lore)
    suspend fun update(lore: Lore)
    suspend fun delete(id: String)
}

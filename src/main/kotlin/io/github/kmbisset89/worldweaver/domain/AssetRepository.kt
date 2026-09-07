package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface AssetRepository {
    fun observeByWorld(worldId: String): Flow<List<Asset>>
    suspend fun getById(id: String): Asset?
    suspend fun getByWorld(worldId: String): List<Asset>
    suspend fun search(query: String): List<Asset>
    suspend fun insert(asset: Asset)
    suspend fun update(asset: Asset)
    suspend fun delete(id: String)
}

package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeAssetRepository : AssetRepository {
    private val assets = MutableStateFlow<List<Asset>>(emptyList())

    fun all(): List<Asset> = assets.value

    override fun observeByWorld(worldId: String): Flow<List<Asset>> {
        return assets.map { list -> list.filter { it.worldId == worldId } }
    }

    override suspend fun getById(id: String): Asset? {
        return assets.value.firstOrNull { it.id == id }
    }

    override suspend fun getByWorld(worldId: String): List<Asset> {
        return assets.value.filter { it.worldId == worldId }
    }

    override suspend fun search(query: String): List<Asset> {
        return assets.value.filter { asset ->
            asset.displayName.contains(query, ignoreCase = true) ||
                asset.originalFileName.contains(query, ignoreCase = true) ||
                asset.notes.contains(query, ignoreCase = true)
        }
    }

    override suspend fun insert(asset: Asset) {
        assets.value = assets.value + asset
    }

    override suspend fun update(asset: Asset) {
        assets.value = assets.value.map { current ->
            if (current.id == asset.id) asset else current
        }
    }

    override suspend fun delete(id: String) {
        assets.value = assets.value.filterNot { it.id == id }
    }
}

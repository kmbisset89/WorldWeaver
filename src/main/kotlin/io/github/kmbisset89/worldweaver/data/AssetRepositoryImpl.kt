package io.github.kmbisset89.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import io.github.kmbisset89.worldweaver.domain.Asset
import io.github.kmbisset89.worldweaver.domain.AssetRepository

internal class AssetRepositoryImpl(
    private val dao: AssetDao,
    private val converter: AssetEntityConverter,
) : AssetRepository {
    override fun observeByWorld(worldId: String): Flow<List<Asset>> {
        return dao.observeByWorld(worldId).map { entities ->
            entities.map(converter::toAsset)
        }
    }

    override suspend fun getById(id: String): Asset? {
        return dao.getById(id)?.let(converter::toAsset)
    }

    override suspend fun getByWorld(worldId: String): List<Asset> {
        return dao.getByWorld(worldId).map(converter::toAsset)
    }

    override suspend fun search(query: String): List<Asset> {
        return dao.searchLike(query).map(converter::toAsset)
    }

    override suspend fun insert(asset: Asset) {
        dao.insert(converter.toEntity(asset))
    }

    override suspend fun update(asset: Asset) {
        dao.update(converter.toEntity(asset))
    }

    override suspend fun delete(id: String) {
        dao.delete(id)
    }
}

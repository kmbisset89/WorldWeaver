package io.github.kmbisset89.worldweaver.data

import io.github.kmbisset89.worldweaver.domain.Asset
import java.time.Instant

internal class AssetEntityConverter {
    fun toAsset(entity: AssetEntity): Asset {
        return Asset(
            id = entity.id,
            worldId = entity.worldId,
            displayName = entity.displayName,
            originalFileName = entity.originalFileName,
            notes = entity.notes,
            byteSize = entity.byteSize,
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMillis),
            updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis),
        )
    }

    fun toEntity(asset: Asset): AssetEntity {
        return AssetEntity(
            id = asset.id,
            worldId = asset.worldId,
            displayName = asset.displayName,
            originalFileName = asset.originalFileName,
            notes = asset.notes,
            byteSize = asset.byteSize,
            createdAtEpochMillis = asset.createdAt.toEpochMilli(),
            updatedAtEpochMillis = asset.updatedAt.toEpochMilli(),
        )
    }
}

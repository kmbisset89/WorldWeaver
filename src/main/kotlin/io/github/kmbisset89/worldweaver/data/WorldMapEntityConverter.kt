package io.github.kmbisset89.worldweaver.data

import io.github.kmbisset89.worldweaver.domain.WorldMap
import java.time.Instant

internal class WorldMapEntityConverter {
    fun toWorldMap(entity: WorldMapEntity): WorldMap {
        return WorldMap(
            id = entity.id,
            worldId = entity.worldId,
            locationId = entity.locationId,
            originalWidth = entity.originalWidth,
            originalHeight = entity.originalHeight,
            tileSizePx = entity.tileSizePx,
            minZoom = entity.minZoom,
            maxZoom = entity.maxZoom,
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMillis),
            updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis),
        )
    }

    fun toEntity(worldMap: WorldMap): WorldMapEntity {
        return WorldMapEntity(
            id = worldMap.id,
            worldId = worldMap.worldId,
            locationId = worldMap.locationId,
            originalWidth = worldMap.originalWidth,
            originalHeight = worldMap.originalHeight,
            tileSizePx = worldMap.tileSizePx,
            minZoom = worldMap.minZoom,
            maxZoom = worldMap.maxZoom,
            createdAtEpochMillis = worldMap.createdAt.toEpochMilli(),
            updatedAtEpochMillis = worldMap.updatedAt.toEpochMilli(),
        )
    }
}

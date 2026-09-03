package io.github.kmbisset89.worldweaver.data

import io.github.kmbisset89.worldweaver.domain.BattleMap
import java.time.Instant

internal class BattleMapEntityConverter(
    private val fogCellsConverter: BattleMapFogCellsConverter = BattleMapFogCellsConverter(),
    private val itemsConverter: BattleMapItemsConverter = BattleMapItemsConverter(),
) {
    fun toBattleMap(entity: BattleMapEntity): BattleMap {
        return BattleMap(
            id = entity.id,
            campaignId = entity.campaignId,
            name = entity.name,
            originalWidth = entity.originalWidth,
            originalHeight = entity.originalHeight,
            tileSizePx = entity.tileSizePx,
            minZoom = entity.minZoom,
            maxZoom = entity.maxZoom,
            columns = entity.columns,
            rows = entity.rows,
            unitName = entity.unitName,
            unitsPerTile = entity.unitsPerTile,
            fogEnabled = entity.fogEnabled,
            revealedCells = fogCellsConverter.decode(entity.revealedCells),
            blockedCells = fogCellsConverter.decode(entity.blockedCells),
            difficultCells = fogCellsConverter.decode(entity.difficultCells),
            items = itemsConverter.decode(entity.items),
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMillis),
            updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis),
        )
    }

    fun toEntity(battleMap: BattleMap): BattleMapEntity {
        return BattleMapEntity(
            id = battleMap.id,
            campaignId = battleMap.campaignId,
            name = battleMap.name,
            originalWidth = battleMap.originalWidth,
            originalHeight = battleMap.originalHeight,
            tileSizePx = battleMap.tileSizePx,
            minZoom = battleMap.minZoom,
            maxZoom = battleMap.maxZoom,
            columns = battleMap.columns,
            rows = battleMap.rows,
            unitName = battleMap.unitName,
            unitsPerTile = battleMap.unitsPerTile,
            fogEnabled = battleMap.fogEnabled,
            revealedCells = fogCellsConverter.encode(battleMap.revealedCells),
            blockedCells = fogCellsConverter.encode(battleMap.blockedCells),
            difficultCells = fogCellsConverter.encode(battleMap.difficultCells),
            items = itemsConverter.encode(battleMap.items),
            createdAtEpochMillis = battleMap.createdAt.toEpochMilli(),
            updatedAtEpochMillis = battleMap.updatedAt.toEpochMilli(),
        )
    }
}

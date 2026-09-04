package io.github.kmbisset89.worldweaver.ui.worldmap

import kotlinx.io.Buffer
import io.github.kmbisset89.worldweaver.domain.WorldMap
import io.github.kmbisset89.worldweaver.domain.WorldMapFileStore
import ovh.plrapps.mapcompose.api.addLayer
import ovh.plrapps.mapcompose.core.TileStreamProvider
import ovh.plrapps.mapcompose.ui.state.MapState

internal class WorldMapMapStateFactory(
    private val fileStore: WorldMapFileStore,
) {
    fun create(worldMap: WorldMap): MapState {
        val state = MapState(
            levelCount = worldMap.levelCount,
            fullWidth = worldMap.originalWidth,
            fullHeight = worldMap.originalHeight,
            tileSize = worldMap.tileSizePx,
        )
        state.addLayer(
            TileStreamProvider { row, col, zoomLvl ->
                val dbZoom = if (worldMap.maxZoom > 0) {
                    (worldMap.maxZoom - zoomLvl).coerceAtLeast(0)
                } else {
                    0
                }
                val bytes = fileStore.readTile(worldMap.id, dbZoom, col, row) ?: return@TileStreamProvider null
                Buffer().apply { write(bytes) }
            }
        )
        return state
    }
}

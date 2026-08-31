package net.tactware.worldweaver.ui.maps

import kotlinx.io.Buffer
import net.tactware.worldweaver.domain.BattleMap
import net.tactware.worldweaver.domain.BattleMapFileStore
import net.tactware.worldweaver.domain.BattleMapSituation
import ovh.plrapps.mapcompose.api.addLayer
import ovh.plrapps.mapcompose.api.removeLayer
import ovh.plrapps.mapcompose.core.TileStreamProvider
import ovh.plrapps.mapcompose.ui.state.MapState

internal class BattleMapMapStateFactory(
    private val fileStore: BattleMapFileStore,
    private val fogTileFactory: BattleMapFogTileFactory = BattleMapFogTileFactory(),
) {
    fun create(battleMap: BattleMap): MapState {
        val state = MapState(
            levelCount = battleMap.levelCount,
            fullWidth = battleMap.originalWidth,
            fullHeight = battleMap.originalHeight,
            tileSize = battleMap.tileSizePx,
        )
        state.addLayer(
            tileProvider(battleMap.maxZoom) { zoom, col, row ->
                fileStore.readTile(battleMap.id, zoom, col, row)
            }
        )
        return state
    }

    fun syncSituationLayers(
        mapState: MapState,
        battleMap: BattleMap,
        situations: List<BattleMapSituation>,
        layerIds: MutableMap<String, String>,
        currentSignature: String?,
    ): String {
        val signature = situations.joinToString { situation ->
            "${situation.id}:${situation.visible}:${situation.sortIndex}"
        }
        if (signature == currentSignature) {
            return signature
        }
        layerIds.values.forEach { layerId ->
            mapState.removeLayer(layerId)
        }
        layerIds.clear()
        situations
            .filter { it.visible }
            .sortedBy { it.sortIndex }
            .forEach { situation ->
                val layerId = mapState.addLayer(
                    tileProvider(battleMap.maxZoom) { zoom, col, row ->
                        fileStore.readSituationTile(battleMap.id, situation.id, zoom, col, row)
                    }
                )
                layerIds[situation.id] = layerId
            }
        return signature
    }

    fun syncFogLayer(
        mapState: MapState,
        battleMap: BattleMap,
        opaque: Boolean,
        layerId: String?,
    ): Pair<String?, String> {
        val signature = fogSignature(battleMap, opaque)
        if (layerId != null) {
            mapState.removeLayer(layerId)
        }
        if (!battleMap.fogEnabled) {
            return null to signature
        }
        val nextId = mapState.addLayer(
            tileProvider(battleMap.maxZoom) { dbZoom, col, row ->
                fogTileFactory.tilePng(
                    battleMap = battleMap,
                    dbZoom = dbZoom,
                    col = col,
                    row = row,
                    opaque = opaque,
                )
            }
        )
        return nextId to signature
    }

    private fun fogSignature(battleMap: BattleMap, opaque: Boolean): String {
        val cells = battleMap.revealedCells
            .sortedWith(compareBy({ it.column }, { it.row }))
            .joinToString(";") { "${it.column},${it.row}" }
        return "${battleMap.fogEnabled}:$opaque:${battleMap.columns}x${battleMap.rows}:$cells"
    }

    private fun tileProvider(
        maxZoom: Int,
        readTile: (zoom: Int, col: Int, row: Int) -> ByteArray?,
    ): TileStreamProvider {
        return TileStreamProvider { row, col, zoomLvl ->
            val dbZoom = if (maxZoom > 0) {
                (maxZoom - zoomLvl).coerceAtLeast(0)
            } else {
                0
            }
            val bytes = readTile(dbZoom, col, row) ?: return@TileStreamProvider null
            Buffer().apply { write(bytes) }
        }
    }
}

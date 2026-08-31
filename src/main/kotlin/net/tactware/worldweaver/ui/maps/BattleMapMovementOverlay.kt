package net.tactware.worldweaver.ui.maps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.tactware.worldweaver.domain.BattleMapGridGeometry
import net.tactware.worldweaver.domain.GridCell
import ovh.plrapps.mapcompose.api.addMarker
import ovh.plrapps.mapcompose.api.removeMarker
import ovh.plrapps.mapcompose.ui.state.MapState

internal class BattleMapMovementOverlay {
    private val boundIds = mutableMapOf<MapState, List<String>>()

    fun bind(
        mapState: MapState,
        geometry: BattleMapGridGeometry,
        origin: GridCell?,
        reachable: List<GridCell>,
    ) {
        clear(mapState)
        if (origin == null) {
            return
        }
        val ids = mutableListOf<String>()
        reachable.forEach { cell ->
            val center = geometry.normalizedCenter(cell) ?: return@forEach
            val isOrigin = cell == origin
            val id = if (isOrigin) {
                ORIGIN_ID
            } else {
                "range-${cell.column}-${cell.row}"
            }
            mapState.addMarker(
                id = id,
                x = center.first,
                y = center.second,
                relativeOffset = Offset(-0.5f, -0.5f),
                clickable = false,
            ) {
                if (isOrigin) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(OriginColor, CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(RangeColor, CircleShape)
                    )
                }
            }
            ids.add(id)
        }
        boundIds[mapState] = ids
    }

    fun clear(mapState: MapState) {
        boundIds.remove(mapState)?.forEach { id ->
            mapState.removeMarker(id)
        }
    }

    private companion object {
        const val ORIGIN_ID = "movement-origin"
        val OriginColor = Color(0xE0F59E0B)
        val RangeColor = Color(0xCC22C55E)
    }
}

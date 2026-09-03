package io.github.kmbisset89.worldweaver.ui.maps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.BattleMapGridGeometry
import io.github.kmbisset89.worldweaver.domain.GridCell
import io.github.kmbisset89.worldweaver.domain.GridDistance
import ovh.plrapps.mapcompose.api.addMarker
import ovh.plrapps.mapcompose.api.removeMarker
import ovh.plrapps.mapcompose.ui.state.MapState

internal class BattleMapMeasureOverlay {
    private val boundIds = mutableMapOf<MapState, List<String>>()

    fun bind(
        mapState: MapState,
        geometry: BattleMapGridGeometry,
        origin: GridCell?,
        destination: GridCell?,
        distance: GridDistance?,
        unitName: String,
    ) {
        clear(mapState)
        if (origin == null) {
            return
        }
        val ids = mutableListOf<String>()
        addEndpoint(mapState, geometry, origin, ORIGIN_ID, ids)
        if (destination != null && destination != origin) {
            addEndpoint(mapState, geometry, destination, DEST_ID, ids)
        }
        distance?.path.orEmpty().forEach { cell ->
            if (cell == origin || cell == destination) {
                return@forEach
            }
            val center = geometry.normalizedCenter(cell) ?: return@forEach
            val id = "measure-${cell.column}-${cell.row}"
            mapState.addMarker(
                id = id,
                x = center.first,
                y = center.second,
                relativeOffset = Offset(-0.5f, -0.5f),
                clickable = false,
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(PathColor, CircleShape)
                )
            }
            ids.add(id)
        }
        if (distance != null && destination != null) {
            val labelCell = distance.path.getOrNull(distance.path.size / 2) ?: destination
            val center = geometry.normalizedCenter(labelCell) ?: return
            mapState.addMarker(
                id = LABEL_ID,
                x = center.first,
                y = center.second,
                relativeOffset = Offset(-0.5f, -1.2f),
                clickable = false,
            ) {
                Text(
                    text = "${distance.squares} sq · ${distance.unitsLabel()} $unitName",
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier
                        .background(LabelBackground, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
            ids.add(LABEL_ID)
        }
        boundIds[mapState] = ids
    }

    fun clear(mapState: MapState) {
        boundIds.remove(mapState)?.forEach { id ->
            mapState.removeMarker(id)
        }
    }

    private fun addEndpoint(
        mapState: MapState,
        geometry: BattleMapGridGeometry,
        cell: GridCell,
        id: String,
        ids: MutableList<String>,
    ) {
        val center = geometry.normalizedCenter(cell) ?: return
        mapState.addMarker(
            id = id,
            x = center.first,
            y = center.second,
            relativeOffset = Offset(-0.5f, -0.5f),
            clickable = false,
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(EndpointColor, CircleShape)
            )
        }
        ids.add(id)
    }

    private companion object {
        const val ORIGIN_ID = "measure-origin"
        const val DEST_ID = "measure-destination"
        const val LABEL_ID = "measure-label"
        val EndpointColor = Color(0xF00EA5E9)
        val PathColor = Color(0xE038BDF8)
        val LabelBackground = Color(0xE00C4A6E)
    }
}

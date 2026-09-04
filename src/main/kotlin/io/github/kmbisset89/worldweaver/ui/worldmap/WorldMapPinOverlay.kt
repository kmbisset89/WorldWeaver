package io.github.kmbisset89.worldweaver.ui.worldmap

import androidx.compose.ui.geometry.Offset
import ovh.plrapps.mapcompose.api.addMarker
import ovh.plrapps.mapcompose.api.removeMarker
import ovh.plrapps.mapcompose.ui.state.MapState

internal class WorldMapPinOverlay {
    private val boundIds = mutableMapOf<MapState, List<String>>()

    fun bind(
        mapState: MapState,
        pins: List<WorldMapViewState.Pin>,
        selectedLocationId: String?,
    ) {
        clear(mapState)
        val ids = mutableListOf<String>()
        pins.forEach { pin ->
            val id = markerId(pin.locationId)
            mapState.addMarker(
                id = id,
                x = pin.x,
                y = pin.y,
                relativeOffset = Offset(-0.5f, -1f),
                zIndex = 2f,
                clickable = true,
            ) {
                WorldMapPinComposeWidget(
                    name = pin.name,
                    hasMap = pin.hasMap,
                    selected = pin.locationId == selectedLocationId,
                )
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

    companion object {
        const val ID_PREFIX = "world-pin-"

        fun markerId(locationId: String): String {
            return ID_PREFIX + locationId
        }

        fun locationIdFrom(markerId: String): String? {
            if (!markerId.startsWith(ID_PREFIX)) {
                return null
            }
            return markerId.removePrefix(ID_PREFIX)
        }
    }
}

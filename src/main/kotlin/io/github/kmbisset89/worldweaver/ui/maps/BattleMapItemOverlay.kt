package io.github.kmbisset89.worldweaver.ui.maps

import androidx.compose.ui.geometry.Offset
import io.github.kmbisset89.worldweaver.domain.BattleMapGridGeometry
import io.github.kmbisset89.worldweaver.domain.BattleMapItem
import ovh.plrapps.mapcompose.api.addMarker
import ovh.plrapps.mapcompose.api.removeMarker
import ovh.plrapps.mapcompose.ui.state.MapState

internal class BattleMapItemOverlay {
    private val boundIds = mutableMapOf<MapState, List<String>>()

    fun bind(
        mapState: MapState,
        geometry: BattleMapGridGeometry,
        items: List<BattleMapItem>,
        selectedItemId: String?,
    ) {
        clear(mapState)
        val ids = mutableListOf<String>()
        items.forEach { item ->
            val center = geometry.normalizedCenter(item.cell) ?: return@forEach
            val id = markerId(item.id)
            mapState.addMarker(
                id = id,
                x = center.first,
                y = center.second,
                relativeOffset = Offset(-0.5f, -0.5f),
                zIndex = 1.5f,
                clickable = true,
            ) {
                BattleMapItemComposeWidget(
                    name = item.name,
                    selected = item.id == selectedItemId,
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
        const val ID_PREFIX = "item-"

        fun markerId(itemId: String): String {
            return ID_PREFIX + itemId
        }

        fun itemIdFrom(markerId: String): String? {
            if (!markerId.startsWith(ID_PREFIX)) {
                return null
            }
            return markerId.removePrefix(ID_PREFIX)
        }
    }
}

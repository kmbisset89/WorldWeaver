package net.tactware.worldweaver.ui.maps

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import net.tactware.worldweaver.domain.BattleMapGridGeometry
import ovh.plrapps.mapcompose.api.addMarker
import ovh.plrapps.mapcompose.api.removeMarker
import ovh.plrapps.mapcompose.ui.state.MapState

internal class BattleMapTokenOverlay {
    private val boundIds = mutableMapOf<MapState, List<String>>()

    fun bind(
        mapState: MapState,
        geometry: BattleMapGridGeometry,
        tokens: List<BattleMapBoardToken>,
    ) {
        clear(mapState)
        val ids = mutableListOf<String>()
        tokens.forEach { token ->
            val center = geometry.normalizedCenter(token.cell) ?: return@forEach
            val id = markerId(token.participantId)
            mapState.addMarker(
                id = id,
                x = center.first,
                y = center.second,
                relativeOffset = Offset(-0.5f, -0.5f),
                zIndex = 2f,
                clickable = true,
            ) {
                BattleMapTokenComposeWidget(
                    name = token.name,
                    avatarPath = token.avatarPath,
                    combatState = token.combatState,
                    conditions = token.conditions,
                    selected = token.selected || token.isCurrentTurn,
                    hiddenFromPlayers = !token.visibleToPlayers,
                    size = if (token.selected || token.isCurrentTurn) 56.dp else 48.dp,
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
        const val ID_PREFIX = "token-"

        fun markerId(participantId: String): String {
            return ID_PREFIX + participantId
        }

        fun participantIdFrom(markerId: String): String? {
            if (!markerId.startsWith(ID_PREFIX)) {
                return null
            }
            return markerId.removePrefix(ID_PREFIX)
        }
    }
}

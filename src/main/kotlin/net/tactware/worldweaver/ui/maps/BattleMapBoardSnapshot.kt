package net.tactware.worldweaver.ui.maps

import net.tactware.worldweaver.domain.GridCell
import net.tactware.worldweaver.domain.GridDistance

internal data class BattleMapBoardSnapshot(
    val tokens: List<BattleMapBoardToken>,
    val selectedTokenParticipantId: String?,
    val selectedTokenName: String?,
    val unplacedTokenCount: Int,
    val movementSpeedText: String,
    val movementOrigin: GridCell?,
    val reachableCells: List<GridCell>,
    val measureEnabled: Boolean,
    val measureOrigin: GridCell?,
    val measureDestination: GridCell?,
    val measureDistance: GridDistance?,
    val fogPaintEnabled: Boolean,
    val fogRevealBrush: Boolean,
    val terrainPaint: TerrainPaintKind?,
    val itemDropEnabled: Boolean,
    val itemNameText: String,
    val selectedItemId: String?,
    val selectedItemName: String?,
    val playerViewOpen: Boolean,
)

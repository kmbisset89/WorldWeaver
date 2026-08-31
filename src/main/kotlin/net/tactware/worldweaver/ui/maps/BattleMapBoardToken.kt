package net.tactware.worldweaver.ui.maps

import net.tactware.worldweaver.domain.CombatState
import net.tactware.worldweaver.domain.GridCell

internal data class BattleMapBoardToken(
    val participantId: String,
    val name: String,
    val cell: GridCell,
    val avatarPath: String?,
    val selected: Boolean,
    val isCurrentTurn: Boolean,
    val combatState: CombatState,
    val conditions: List<String>,
    val visibleToPlayers: Boolean,
)

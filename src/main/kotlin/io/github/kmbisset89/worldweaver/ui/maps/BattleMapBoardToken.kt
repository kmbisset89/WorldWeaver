package io.github.kmbisset89.worldweaver.ui.maps

import io.github.kmbisset89.worldweaver.domain.CombatState
import io.github.kmbisset89.worldweaver.domain.GridCell

internal data class BattleMapBoardToken(
    val participantId: String,
    val name: String,
    val cell: GridCell,
    val span: Int = 1,
    val avatarPath: String?,
    val selected: Boolean,
    val isCurrentTurn: Boolean,
    val combatState: CombatState,
    val conditions: List<String>,
    val visibleToPlayers: Boolean,
)

package io.github.kmbisset89.worldweaver.ui.maps

internal data class BattleMapBoardOutcome(
    val snapshot: BattleMapBoardSnapshot,
    val work: BattleMapBoardWork? = null,
)

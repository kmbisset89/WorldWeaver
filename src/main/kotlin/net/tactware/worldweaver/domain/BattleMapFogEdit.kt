package net.tactware.worldweaver.domain

internal sealed interface BattleMapFogEdit {
    data class Reveal(val cells: Set<GridCell>) : BattleMapFogEdit
    data class Hide(val cells: Set<GridCell>) : BattleMapFogEdit
    data object RevealAll : BattleMapFogEdit
    data object HideAll : BattleMapFogEdit
}

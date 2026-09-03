package net.tactware.worldweaver.domain

internal sealed interface BattleMapTerrainEdit {
    data class SetBlocked(val cells: Set<GridCell>) : BattleMapTerrainEdit

    data class SetDifficult(val cells: Set<GridCell>) : BattleMapTerrainEdit

    data class Clear(val cells: Set<GridCell>) : BattleMapTerrainEdit
}

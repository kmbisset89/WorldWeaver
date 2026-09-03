package net.tactware.worldweaver.domain

internal data class BattleMapItem(
    val id: String,
    val name: String,
    val cell: GridCell,
)

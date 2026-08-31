package net.tactware.worldweaver.domain

internal data class BattleMapDraft(
    val name: String,
    val imagePng: ByteArray,
    val columns: Int,
    val rows: Int,
    val unitName: String,
    val unitsPerTile: Double,
)

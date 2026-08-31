package net.tactware.worldweaver.domain

import java.time.Instant

internal data class BattleMap(
    val id: String,
    val campaignId: String,
    val name: String,
    val originalWidth: Int,
    val originalHeight: Int,
    val tileSizePx: Int,
    val minZoom: Int,
    val maxZoom: Int,
    val columns: Int = 20,
    val rows: Int = 20,
    val unitName: String = "ft",
    val unitsPerTile: Double = 5.0,
    val fogEnabled: Boolean = false,
    val revealedCells: Set<GridCell> = emptySet(),
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val levelCount: Int
        get() = (maxZoom - minZoom + 1).coerceAtLeast(1)

    fun isRevealedToPlayers(cell: GridCell): Boolean {
        return !fogEnabled || cell in revealedCells
    }

    fun allCells(): Set<GridCell> {
        return buildSet {
            for (column in 0 until columns) {
                for (row in 0 until rows) {
                    add(GridCell(column = column, row = row))
                }
            }
        }
    }
}

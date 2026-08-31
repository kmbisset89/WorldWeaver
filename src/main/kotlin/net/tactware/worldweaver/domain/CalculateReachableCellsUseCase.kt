package net.tactware.worldweaver.domain

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max

internal class CalculateReachableCellsUseCase {

    operator fun invoke(
        origin: GridCell,
        walkSpeed: Int,
        unitsPerTile: Double,
        columns: Int,
        rows: Int,
    ): List<GridCell> {
        if (columns < 1 || rows < 1) {
            return emptyList()
        }
        if (origin.column !in 0 until columns || origin.row !in 0 until rows) {
            return emptyList()
        }
        val budget = if (unitsPerTile > 0.0) {
            floor(walkSpeed.coerceAtLeast(0) / unitsPerTile).toInt()
        } else {
            0
        }
        val cells = mutableListOf<GridCell>()
        for (column in 0 until columns) {
            for (row in 0 until rows) {
                val distance = max(abs(column - origin.column), abs(row - origin.row))
                if (distance <= budget) {
                    cells.add(GridCell(column = column, row = row))
                }
            }
        }
        return cells
    }
}

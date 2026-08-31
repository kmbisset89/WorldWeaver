package net.tactware.worldweaver.domain

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign

internal class CalculateGridDistanceUseCase {

    operator fun invoke(
        from: GridCell,
        to: GridCell,
        unitsPerTile: Double,
    ): GridDistance {
        val squares = max(abs(to.column - from.column), abs(to.row - from.row))
        return GridDistance(
            squares = squares,
            units = squares * unitsPerTile.coerceAtLeast(0.0),
            path = path(from, to),
        )
    }

    private fun path(from: GridCell, to: GridCell): List<GridCell> {
        val cells = mutableListOf(from)
        var column = from.column
        var row = from.row
        val stepColumn = sign((to.column - from.column).toDouble()).toInt()
        val stepRow = sign((to.row - from.row).toDouble()).toInt()
        while (column != to.column || row != to.row) {
            if (column != to.column) {
                column += stepColumn
            }
            if (row != to.row) {
                row += stepRow
            }
            cells.add(GridCell(column = column, row = row))
        }
        return cells
    }
}

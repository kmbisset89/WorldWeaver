package net.tactware.worldweaver.data

import net.tactware.worldweaver.domain.GridCell

internal class BattleMapFogCellsConverter {
    fun encode(cells: Set<GridCell>): String {
        return cells
            .sortedWith(compareBy({ it.column }, { it.row }))
            .joinToString(";") { cell -> "${cell.column},${cell.row}" }
    }

    fun decode(raw: String): Set<GridCell> {
        if (raw.isBlank()) {
            return emptySet()
        }
        return raw.split(';')
            .mapNotNull { part ->
                val bits = part.split(',')
                if (bits.size != 2) {
                    return@mapNotNull null
                }
                val column = bits[0].toIntOrNull() ?: return@mapNotNull null
                val row = bits[1].toIntOrNull() ?: return@mapNotNull null
                GridCell(column = column, row = row)
            }
            .toSet()
    }
}

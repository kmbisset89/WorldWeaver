package net.tactware.worldweaver.domain

internal enum class CreatureSize(
    val displayName: String,
    val span: Int,
) {
    Tiny("Tiny", 1),
    Small("Small", 1),
    Medium("Medium", 1),
    Large("Large", 2),
    Huge("Huge", 3),
    Gargantuan("Gargantuan", 4),
    ;

    fun occupiedCells(origin: GridCell): List<GridCell> {
        return buildList {
            for (columnOffset in 0 until span) {
                for (rowOffset in 0 until span) {
                    add(
                        GridCell(
                            column = origin.column + columnOffset,
                            row = origin.row + rowOffset,
                        ),
                    )
                }
            }
        }
    }

    companion object {
        fun fromStorage(value: String): CreatureSize {
            return entries.firstOrNull { it.name == value } ?: Medium
        }
    }
}

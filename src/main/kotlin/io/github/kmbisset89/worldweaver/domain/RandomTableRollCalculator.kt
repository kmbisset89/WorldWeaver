package io.github.kmbisset89.worldweaver.domain

import kotlin.random.Random

internal class RandomTableRollCalculator(
    private val nextInt: (Int) -> Int = { bound -> Random.nextInt(bound) },
) {
    fun roll(
        table: RandomTable,
        tablesById: Map<String, RandomTable>,
    ): RandomTableRoll? {
        return rollNested(table, tablesById, visited = emptySet())
    }

    private fun rollNested(
        table: RandomTable,
        tablesById: Map<String, RandomTable>,
        visited: Set<String>,
    ): RandomTableRoll? {
        if (table.id in visited) {
            return null
        }
        val eligible = table.rows.filter { row ->
            row.label.isNotBlank() && row.weight > 0
        }
        if (eligible.isEmpty()) {
            return null
        }
        val total = eligible.sumOf { it.weight }
        var pick = nextInt(total)
        val chosen = eligible.first { row ->
            pick -= row.weight
            pick < 0
        }
        val nestedTable = chosen.nestedTableId
            ?.takeIf { nestedId -> nestedId != table.id }
            ?.let(tablesById::get)
        val nested = nestedTable?.let { child ->
            rollNested(child, tablesById, visited + table.id)
        }
        return RandomTableRoll(
            tableId = table.id,
            tableName = table.name,
            label = chosen.label,
            nested = nested,
        )
    }
}

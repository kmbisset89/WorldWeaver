package io.github.kmbisset89.worldweaver.domain

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class RandomTableRollCalculatorTest {
    @Test
    fun picksTheOnlyWeightedRow() {
        val table = table(
            id = "weather",
            rows = listOf(
                RandomTableRow("r1", "Clear", 1, null),
            ),
        )
        val roll = RandomTableRollCalculator { 0 }.roll(table, mapOf(table.id to table))
        assertEquals("Clear", roll?.label)
        assertNull(roll?.nested)
    }

    @Test
    fun followsANestedTable() {
        val nested = table(
            id = "loot",
            rows = listOf(RandomTableRow("n1", "Potion", 1, null)),
        )
        val outer = table(
            id = "encounters",
            rows = listOf(RandomTableRow("o1", "Chest", 1, nested.id)),
        )
        val roll = RandomTableRollCalculator { 0 }.roll(
            outer,
            mapOf(outer.id to outer, nested.id to nested),
        )
        assertEquals("Chest", roll?.label)
        assertEquals("Potion", roll?.nested?.label)
        assertEquals("Chest → Potion", roll?.displayText())
    }

    @Test
    fun skipsCyclicNesting() {
        val a = table(
            id = "a",
            rows = listOf(RandomTableRow("a1", "Loop", 1, "b")),
        )
        val b = table(
            id = "b",
            rows = listOf(RandomTableRow("b1", "Back", 1, "a")),
        )
        val roll = RandomTableRollCalculator { 0 }.roll(a, mapOf(a.id to a, b.id to b))
        assertEquals("Loop", roll?.label)
        assertEquals("Back", roll?.nested?.label)
        assertNull(roll?.nested?.nested)
    }

    private fun table(
        id: String,
        rows: List<RandomTableRow>,
    ): RandomTable {
        val now = Instant.parse("2026-09-06T12:00:00Z")
        return RandomTable(
            id = id,
            worldId = "world-1",
            name = id,
            notes = "",
            rows = rows,
            createdAt = now,
            updatedAt = now,
        )
    }
}

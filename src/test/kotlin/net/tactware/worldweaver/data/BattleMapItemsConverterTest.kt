package net.tactware.worldweaver.data

import net.tactware.worldweaver.domain.BattleMapItem
import net.tactware.worldweaver.domain.GridCell
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BattleMapItemsConverterTest {
    private val converter = BattleMapItemsConverter()

    @Test
    fun emptyEncodesBlank() {
        assertEquals("", converter.encode(emptyList()))
        assertEquals(emptyList(), converter.decode(""))
        assertEquals(emptyList(), converter.decode("   "))
    }

    @Test
    fun roundTripsNamedItems() {
        val items = listOf(
            BattleMapItem(id = "item-1", name = "Rusty sword", cell = GridCell(1, 2)),
            BattleMapItem(id = "item-2", name = "Torch", cell = GridCell(0, 0)),
        )
        assertEquals(items, converter.decode(converter.encode(items)))
    }
}

package net.tactware.worldweaver.data

import net.tactware.worldweaver.domain.GridCell
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BattleMapFogCellsConverterTest {
    private val converter = BattleMapFogCellsConverter()

    @Test
    fun encodeSortsAndRoundTrips() {
        val cells = setOf(GridCell(2, 1), GridCell(0, 0), GridCell(1, 4))
        val encoded = converter.encode(cells)
        assertEquals("0,0;1,4;2,1", encoded)
        assertEquals(cells, converter.decode(encoded))
    }

    @Test
    fun blankDecodesEmpty() {
        assertEquals(emptySet(), converter.decode(""))
        assertEquals(emptySet(), converter.decode("   "))
    }
}

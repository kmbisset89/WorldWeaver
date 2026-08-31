package net.tactware.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class BattleMapGridGeometryTest {
    @Test
    fun remainderColumnsWidenTheFirstCells() {
        val geometry = BattleMapGridGeometry(
            imageWidth = 10,
            imageHeight = 10,
            columns = 3,
            rows = 3,
        )

        assertEquals(GridCell(0, 0), geometry.cellAtNormalized(0.15, 0.15))
        assertEquals(GridCell(1, 0), geometry.cellAtNormalized(0.45, 0.15))
        assertEquals(GridCell(2, 2), geometry.cellAtNormalized(0.99, 0.99))
    }

    @Test
    fun tapOnTheSharedEdgeUsesTheNextCell() {
        val geometry = BattleMapGridGeometry(
            imageWidth = 10,
            imageHeight = 10,
            columns = 3,
            rows = 1,
        )

        assertEquals(GridCell(0, 0), geometry.cellAtNormalized(0.39, 0.5))
        assertEquals(GridCell(1, 0), geometry.cellAtNormalized(0.4, 0.5))
    }

    @Test
    fun normalizedCenterSitsInsideTheCell() {
        val geometry = BattleMapGridGeometry(
            imageWidth = 10,
            imageHeight = 10,
            columns = 2,
            rows = 2,
        )

        val center = geometry.normalizedCenter(GridCell(0, 0))
        assertNotNull(center)
        assertEquals(GridCell(0, 0), geometry.cellAtNormalized(center.first, center.second))
    }

    @Test
    fun invalidGridReturnsNull() {
        val geometry = BattleMapGridGeometry(
            imageWidth = 0,
            imageHeight = 10,
            columns = 2,
            rows = 2,
        )

        assertNull(geometry.cellAtNormalized(0.5, 0.5))
        assertNull(geometry.normalizedCenter(GridCell(0, 0)))
    }
}

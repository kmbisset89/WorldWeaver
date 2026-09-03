package net.tactware.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class CalculateReachableCellsUseCaseTest {
    private val calculateReachableCells = CalculateReachableCellsUseCase()

    @Test
    fun thirtyFeetOnAFiveFootGridIsSixSquaresIncludingOrigin() {
        val origin = GridCell(column = 10, row = 10)
        val cells = calculateReachableCells(
            origin = origin,
            walkSpeed = 30,
            unitsPerTile = 5.0,
            columns = 21,
            rows = 21,
        )

        assertTrue(cells.contains(origin))
        assertEquals(13 * 13, cells.size)
        assertTrue(cells.contains(GridCell(16, 10)))
        assertTrue(cells.contains(GridCell(16, 16)))
        assertTrue(cells.none { it.column == 17 && it.row == 10 })
    }

    @Test
    fun rangeStopsAtMapBounds() {
        val cells = calculateReachableCells(
            origin = GridCell(column = 0, row = 0),
            walkSpeed = 30,
            unitsPerTile = 5.0,
            columns = 3,
            rows = 3,
        )

        assertEquals(9, cells.size)
        assertTrue(cells.contains(GridCell(0, 0)))
        assertTrue(cells.contains(GridCell(2, 2)))
    }

    @Test
    fun zeroSpeedReturnsOnlyTheOrigin() {
        val origin = GridCell(column = 2, row = 2)
        val cells = calculateReachableCells(
            origin = origin,
            walkSpeed = 0,
            unitsPerTile = 5.0,
            columns = 5,
            rows = 5,
        )

        assertEquals(listOf(origin), cells)
    }

    @Test
    fun originOutsideTheMapIsEmpty() {
        val cells = calculateReachableCells(
            origin = GridCell(column = 8, row = 0),
            walkSpeed = 30,
            unitsPerTile = 5.0,
            columns = 4,
            rows = 4,
        )

        assertTrue(cells.isEmpty())
    }

    @Test
    fun blockedCellsStopWalk() {
        val origin = GridCell(column = 2, row = 2)
        val cells = calculateReachableCells(
            origin = origin,
            walkSpeed = 30,
            unitsPerTile = 5.0,
            columns = 5,
            rows = 5,
            blockedCells = setOf(GridCell(column = 3, row = 2)),
        )

        assertTrue(cells.contains(origin))
        assertTrue(cells.none { it.column == 3 && it.row == 2 })
        assertTrue(cells.contains(GridCell(column = 4, row = 2)))
    }

    @Test
    fun difficultCellsCostTwoSquares() {
        val origin = GridCell(column = 0, row = 0)
        val cells = calculateReachableCells(
            origin = origin,
            walkSpeed = 10,
            unitsPerTile = 5.0,
            columns = 4,
            rows = 1,
            difficultCells = setOf(GridCell(column = 1, row = 0)),
        )

        assertEquals(
            setOf(origin, GridCell(column = 1, row = 0)),
            cells.toSet(),
        )
    }

    @Test
    fun occupiedCellsBlockWalkExceptOrigin() {
        val origin = GridCell(column = 1, row = 1)
        val cells = calculateReachableCells(
            origin = origin,
            walkSpeed = 30,
            unitsPerTile = 5.0,
            columns = 3,
            rows = 3,
            occupiedCells = setOf(GridCell(column = 2, row = 1)),
        )

        assertTrue(cells.contains(origin))
        assertTrue(cells.none { it == GridCell(column = 2, row = 1) })
    }
}

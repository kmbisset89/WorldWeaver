package net.tactware.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals

internal class CalculateGridDistanceUseCaseTest {
    private val calculateGridDistance = CalculateGridDistanceUseCase()

    @Test
    fun sameCellIsZeroSquares() {
        val cell = GridCell(column = 4, row = 4)
        val distance = calculateGridDistance(from = cell, to = cell, unitsPerTile = 5.0)
        assertEquals(0, distance.squares)
        assertEquals(0.0, distance.units)
        assertEquals(listOf(cell), distance.path)
    }

    @Test
    fun orthogonalThreeSquaresIsFifteenFeet() {
        val from = GridCell(column = 2, row = 5)
        val to = GridCell(column = 5, row = 5)
        val distance = calculateGridDistance(from = from, to = to, unitsPerTile = 5.0)
        assertEquals(3, distance.squares)
        assertEquals(15.0, distance.units)
        assertEquals("15", distance.unitsLabel())
        assertEquals(
            listOf(
                GridCell(2, 5),
                GridCell(3, 5),
                GridCell(4, 5),
                GridCell(5, 5),
            ),
            distance.path,
        )
    }

    @Test
    fun diagonalUsesChebyshevSquares() {
        val from = GridCell(column = 0, row = 0)
        val to = GridCell(column = 3, row = 3)
        val distance = calculateGridDistance(from = from, to = to, unitsPerTile = 5.0)
        assertEquals(3, distance.squares)
        assertEquals(15.0, distance.units)
        assertEquals(
            listOf(
                GridCell(0, 0),
                GridCell(1, 1),
                GridCell(2, 2),
                GridCell(3, 3),
            ),
            distance.path,
        )
    }

    @Test
    fun longerAxisStillCountsOncePerSquare() {
        val from = GridCell(column = 1, row = 1)
        val to = GridCell(column = 4, row = 2)
        val distance = calculateGridDistance(from = from, to = to, unitsPerTile = 5.0)
        assertEquals(3, distance.squares)
        assertEquals(15.0, distance.units)
        assertEquals(4, distance.path.size)
        assertEquals(from, distance.path.first())
        assertEquals(to, distance.path.last())
    }
}

package io.github.kmbisset89.worldweaver.domain

import java.util.ArrayDeque
import kotlin.math.floor

internal class CalculateReachableCellsUseCase {

    operator fun invoke(
        origin: GridCell,
        walkSpeed: Int,
        unitsPerTile: Double,
        columns: Int,
        rows: Int,
        blockedCells: Set<GridCell> = emptySet(),
        difficultCells: Set<GridCell> = emptySet(),
        occupiedCells: Set<GridCell> = emptySet(),
    ): List<GridCell> {
        if (columns < 1 || rows < 1) {
            return emptyList()
        }
        if (origin.column !in 0 until columns || origin.row !in 0 until rows) {
            return emptyList()
        }
        val budget = if (unitsPerTile > 0.0) {
            floor(walkSpeed.coerceAtLeast(0) / unitsPerTile).toInt()
        } else {
            0
        }
        val impassable = blockedCells + occupiedCells - origin
        val reachable = linkedSetOf(origin)
        val cost = mutableMapOf(origin to 0)
        val queue = ArrayDeque<GridCell>()
        queue.add(origin)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val spent = cost.getValue(current)
            if (spent >= budget) {
                continue
            }
            for (columnOffset in -1..1) {
                for (rowOffset in -1..1) {
                    if (columnOffset == 0 && rowOffset == 0) {
                        continue
                    }
                    val next = GridCell(
                        column = current.column + columnOffset,
                        row = current.row + rowOffset,
                    )
                    if (next.column !in 0 until columns || next.row !in 0 until rows) {
                        continue
                    }
                    if (next in impassable) {
                        continue
                    }
                    val stepCost = if (next in difficultCells) 2 else 1
                    val nextCost = spent + stepCost
                    if (nextCost > budget) {
                        continue
                    }
                    val previous = cost[next]
                    if (previous != null && previous <= nextCost) {
                        continue
                    }
                    cost[next] = nextCost
                    reachable.add(next)
                    queue.add(next)
                }
            }
        }
        return reachable.toList()
    }
}

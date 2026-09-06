package io.github.kmbisset89.worldweaver.ui.maps

import io.github.kmbisset89.worldweaver.domain.BattleMapFogEdit
import io.github.kmbisset89.worldweaver.domain.BattleMapTerrainEdit
import io.github.kmbisset89.worldweaver.domain.GridCell

internal sealed interface BattleMapBoardWork {

    data class UpdateFog(
        val mapId: String,
        val edit: BattleMapFogEdit,
    ) : BattleMapBoardWork

    data class UpdateTerrain(
        val mapId: String,
        val edit: BattleMapTerrainEdit,
    ) : BattleMapBoardWork

    data class PlaceItem(
        val mapId: String,
        val name: String,
        val cell: GridCell,
    ) : BattleMapBoardWork

    data class PlaceToken(
        val encounterId: String,
        val participantId: String,
        val cell: GridCell,
        val columns: Int,
        val rows: Int,
        val span: Int,
    ) : BattleMapBoardWork

    data class DeleteItem(
        val mapId: String,
        val itemId: String,
    ) : BattleMapBoardWork

    data class ComputeReachableCells(
        val origin: GridCell,
        val walkSpeed: Int,
        val unitsPerTile: Double,
        val columns: Int,
        val rows: Int,
        val blockedCells: Set<GridCell>,
        val difficultCells: Set<GridCell>,
        val occupiedCells: Set<GridCell>,
    ) : BattleMapBoardWork

    data class ComputeMeasureDistance(
        val from: GridCell,
        val to: GridCell,
        val unitsPerTile: Double,
    ) : BattleMapBoardWork
}

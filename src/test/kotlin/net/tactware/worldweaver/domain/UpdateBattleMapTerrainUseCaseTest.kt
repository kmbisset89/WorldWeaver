package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class UpdateBattleMapTerrainUseCaseTest {
    @Test
    fun setDifficultAddsCells() = runTest {
        val harness = Harness()
        harness.insertMap()

        assertIs<UpdateBattleMapTerrainUseCase.Result.Updated>(
            harness.updateTerrain(BattleMapTerrainEdit.SetDifficult(setOf(GridCell(1, 1)))),
        )
        val map = harness.maps.getById("map-1")!!
        assertEquals(setOf(GridCell(1, 1)), map.difficultCells)
        assertEquals(emptySet(), map.blockedCells)
    }

    @Test
    fun setBlockedReplacesDifficultOnTheSameCell() = runTest {
        val harness = Harness()
        harness.insertMap()
        harness.updateTerrain(BattleMapTerrainEdit.SetDifficult(setOf(GridCell(0, 0))))

        harness.updateTerrain(BattleMapTerrainEdit.SetBlocked(setOf(GridCell(0, 0))))

        val map = harness.maps.getById("map-1")!!
        assertEquals(setOf(GridCell(0, 0)), map.blockedCells)
        assertEquals(emptySet(), map.difficultCells)
    }

    @Test
    fun setDifficultReplacesBlockedOnTheSameCell() = runTest {
        val harness = Harness()
        harness.insertMap()
        harness.updateTerrain(BattleMapTerrainEdit.SetBlocked(setOf(GridCell(2, 0))))

        harness.updateTerrain(BattleMapTerrainEdit.SetDifficult(setOf(GridCell(2, 0))))

        val map = harness.maps.getById("map-1")!!
        assertEquals(setOf(GridCell(2, 0)), map.difficultCells)
        assertEquals(emptySet(), map.blockedCells)
    }

    @Test
    fun clearRemovesBlockedAndDifficult() = runTest {
        val harness = Harness()
        harness.insertMap()
        harness.updateTerrain(BattleMapTerrainEdit.SetBlocked(setOf(GridCell(0, 0))))
        harness.updateTerrain(BattleMapTerrainEdit.SetDifficult(setOf(GridCell(1, 0))))

        harness.updateTerrain(BattleMapTerrainEdit.Clear(setOf(GridCell(0, 0), GridCell(1, 0))))

        val map = harness.maps.getById("map-1")!!
        assertEquals(emptySet(), map.blockedCells)
        assertEquals(emptySet(), map.difficultCells)
    }

    @Test
    fun missingMapIsNotFound() = runTest {
        val harness = Harness()
        val result = harness.updateTerrain(BattleMapTerrainEdit.SetDifficult(setOf(GridCell(0, 0))))
        assertIs<UpdateBattleMapTerrainUseCase.Result.NotFound>(result)
    }

    private class Harness {
        val maps = FakeBattleMapRepository()
        private val updateTerrain = UpdateBattleMapTerrainUseCase(
            battleMapRepository = maps,
            instantProvider = InstantProvider { Instant.parse("2026-08-30T18:00:00Z") },
        )

        suspend fun insertMap() {
            maps.insert(
                BattleMap(
                    id = "map-1",
                    campaignId = "campaign-1",
                    name = "Cave",
                    originalWidth = 512,
                    originalHeight = 512,
                    tileSizePx = 256,
                    minZoom = 0,
                    maxZoom = 0,
                    columns = 3,
                    rows = 3,
                    createdAt = Instant.parse("2026-08-30T12:00:00Z"),
                    updatedAt = Instant.parse("2026-08-30T12:00:00Z"),
                ),
            )
        }

        suspend fun updateTerrain(edit: BattleMapTerrainEdit): UpdateBattleMapTerrainUseCase.Result {
            return updateTerrain("map-1", edit)
        }
    }
}

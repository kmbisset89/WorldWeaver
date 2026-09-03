package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class UpdateBattleMapFogUseCaseTest {
    @Test
    fun hideAllEnablesFogWithNothingRevealed() = runTest {
        val harness = Harness()
        harness.insertMap()

        assertIs<UpdateBattleMapFogUseCase.Result.Updated>(
            harness.updateFog(BattleMapFogEdit.HideAll),
        )
        val map = harness.maps.getById("map-1")!!
        assertTrue(map.fogEnabled)
        assertTrue(map.revealedCells.isEmpty())
        assertFalse(map.isRevealedToPlayers(GridCell(0, 0)))
    }

    @Test
    fun hideACellWhenFogIsOffStartsFromFullyRevealed() = runTest {
        val harness = Harness()
        harness.insertMap(columns = 2, rows = 2)

        assertIs<UpdateBattleMapFogUseCase.Result.Updated>(
            harness.updateFog(BattleMapFogEdit.Hide(setOf(GridCell(0, 0)))),
        )
        val map = harness.maps.getById("map-1")!!
        assertTrue(map.fogEnabled)
        assertEquals(
            setOf(GridCell(0, 1), GridCell(1, 0), GridCell(1, 1)),
            map.revealedCells,
        )
        assertFalse(map.isRevealedToPlayers(GridCell(0, 0)))
        assertTrue(map.isRevealedToPlayers(GridCell(1, 1)))
    }

    @Test
    fun revealCellsThenRevealAllClearsFog() = runTest {
        val harness = Harness()
        harness.insertMap(columns = 2, rows = 2)
        harness.updateFog(BattleMapFogEdit.HideAll)
        harness.updateFog(BattleMapFogEdit.Reveal(setOf(GridCell(1, 1))))

        var map = harness.maps.getById("map-1")!!
        assertTrue(map.fogEnabled)
        assertEquals(setOf(GridCell(1, 1)), map.revealedCells)

        harness.updateFog(BattleMapFogEdit.RevealAll)
        map = harness.maps.getById("map-1")!!
        assertFalse(map.fogEnabled)
        assertTrue(map.revealedCells.isEmpty())
        assertTrue(map.isRevealedToPlayers(GridCell(0, 0)))
    }

    @Test
    fun revealingTheLastHiddenCellTurnsFogOff() = runTest {
        val harness = Harness()
        harness.insertMap(columns = 1, rows = 2)
        harness.updateFog(BattleMapFogEdit.HideAll)
        harness.updateFog(BattleMapFogEdit.Reveal(setOf(GridCell(0, 0), GridCell(0, 1))))

        val map = harness.maps.getById("map-1")!!
        assertFalse(map.fogEnabled)
        assertTrue(map.revealedCells.isEmpty())
    }

    @Test
    fun missingMapIsNotFound() = runTest {
        val harness = Harness()
        val result = harness.updateFog(BattleMapFogEdit.HideAll)
        assertIs<UpdateBattleMapFogUseCase.Result.NotFound>(result)
    }

    private class Harness {
        val maps = FakeBattleMapRepository()
        private val updateFog = UpdateBattleMapFogUseCase(
            battleMapRepository = maps,
            instantProvider = InstantProvider { Instant.parse("2026-08-30T18:00:00Z") },
        )

        suspend fun insertMap(columns: Int = 3, rows: Int = 3) {
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
                    columns = columns,
                    rows = rows,
                    createdAt = Instant.parse("2026-08-30T12:00:00Z"),
                    updatedAt = Instant.parse("2026-08-30T12:00:00Z"),
                ),
            )
        }

        suspend fun updateFog(edit: BattleMapFogEdit): UpdateBattleMapFogUseCase.Result {
            return updateFog("map-1", edit)
        }
    }
}

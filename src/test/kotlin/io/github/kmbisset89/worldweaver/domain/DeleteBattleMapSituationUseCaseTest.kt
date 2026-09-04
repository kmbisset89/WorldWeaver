package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class DeleteBattleMapSituationUseCaseTest {
    @Test
    fun deleteRemovesRowAndFiles() = runTest {
        val harness = Harness()
        val pyramid = MapTilePyramidFactory().create(BattleMapPngFixture.pngBytes(64, 64))!!
        harness.fileStore.writeSituation("map-1", "sit-1", pyramid)
        harness.situations.insert(harness.sample())

        val result = harness.deleteSituation("sit-1")

        assertIs<DeleteBattleMapSituationUseCase.Result.Deleted>(result)
        assertTrue(harness.situations.all().isEmpty())
        assertTrue(!harness.mapsRoot.resolve("map-1/situations/sit-1").toFile().exists())
    }

    @Test
    fun deleteMissingReturnsNotFound() = runTest {
        val harness = Harness()

        val result = harness.deleteSituation("missing")

        assertIs<DeleteBattleMapSituationUseCase.Result.NotFound>(result)
    }

    private class Harness {
        val mapsRoot = Files.createTempDirectory("ww-sit-delete")
        val situations = FakeBattleMapSituationRepository()
        val fileStore = BattleMapFileStore(mapsRoot.toFile())
        val deleteSituation = DeleteBattleMapSituationUseCase(situations, fileStore)

        fun sample(): BattleMapSituation {
            val now = Instant.parse("2026-08-30T12:00:00Z")
            return BattleMapSituation(
                id = "sit-1",
                battleMapId = "map-1",
                name = "Flood",
                visible = true,
                sortIndex = 0,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}

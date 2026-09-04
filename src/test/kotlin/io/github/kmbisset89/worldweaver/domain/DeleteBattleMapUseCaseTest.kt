package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class DeleteBattleMapUseCaseTest {
    @Test
    fun deleteDetachesEncountersAndRemovesFiles() = runTest {
        val harness = Harness()
        harness.fileStore.write(
            "map-1",
            MapTilePyramidFactory().create(BattleMapPngFixture.pngBytes(64, 64))!!,
        )
        harness.battleMaps.insert(harness.sampleMap())
        harness.encounters.insert(harness.sampleEncounter(battleMapId = "map-1"))

        val result = harness.deleteBattleMap("map-1")

        assertIs<DeleteBattleMapUseCase.Result.Deleted>(result)
        assertTrue(harness.battleMaps.all().isEmpty())
        assertNull(harness.encounters.all().single().battleMapId)
        assertTrue(!harness.mapsRoot.resolve("map-1").toFile().exists())
    }

    @Test
    fun deleteMissingMapReturnsNotFound() = runTest {
        val harness = Harness()

        val result = harness.deleteBattleMap("missing")

        assertIs<DeleteBattleMapUseCase.Result.NotFound>(result)
    }

    private class Harness {
        val mapsRoot = Files.createTempDirectory("ww-maps-delete")
        val battleMaps = FakeBattleMapRepository()
        val encounters = FakeEncounterRepository()
        val fileStore = BattleMapFileStore(mapsRoot.toFile())
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        val deleteBattleMap = DeleteBattleMapUseCase(
            battleMaps,
            encounters,
            fileStore,
            instant,
        )

        fun sampleMap(): BattleMap {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            return BattleMap(
                id = "map-1",
                campaignId = "campaign-1",
                name = "Cave",
                originalWidth = 64,
                originalHeight = 64,
                tileSizePx = 256,
                minZoom = 0,
                maxZoom = 0,
                createdAt = now,
                updatedAt = now,
            )
        }

        fun sampleEncounter(battleMapId: String?): Encounter {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            return Encounter(
                id = "enc-1",
                campaignId = "campaign-1",
                name = "Ambush",
                locationId = null,
                battleMapId = battleMapId,
                difficulty = EncounterDifficulty.Medium,
                notes = "",
                outcomeNote = "",
                status = EncounterStatus.Planned,
                currentRound = 1,
                currentTurnIndex = 0,
                participants = emptyList(),
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}

package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class CreateBattleMapSituationUseCaseTest {
    @Test
    fun createRequiresActiveCampaign() = runTest {
        val harness = Harness()
        harness.battleMaps.insert(harness.sampleMap())

        val result = harness.createSituation(harness.draft())

        assertIs<CreateBattleMapSituationUseCase.Result.NoActiveCampaign>(result)
        assertTrue(harness.situations.all().isEmpty())
    }

    @Test
    fun createRejectsMissingMap() = runTest {
        val harness = Harness()
        harness.activateCampaign()

        val result = harness.createSituation(harness.draft())

        assertIs<CreateBattleMapSituationUseCase.Result.MapNotFound>(result)
    }

    @Test
    fun createRejectsBlankName() = runTest {
        val harness = Harness()
        harness.activateCampaign()
        harness.battleMaps.insert(harness.sampleMap())

        val result = harness.createSituation(harness.draft(name = "  "))

        assertIs<CreateBattleMapSituationUseCase.Result.InvalidName>(result)
    }

    @Test
    fun createRejectsUnreadableImage() = runTest {
        val harness = Harness()
        harness.activateCampaign()
        harness.battleMaps.insert(harness.sampleMap())

        val result = harness.createSituation(harness.draft(imagePng = byteArrayOf(1, 2, 3)))

        assertIs<CreateBattleMapSituationUseCase.Result.InvalidImage>(result)
    }

    @Test
    fun createPersistsVisibleLayerAndTilesFittedToMap() = runTest {
        val harness = Harness()
        harness.activateCampaign()
        harness.battleMaps.insert(harness.sampleMap(width = 128, height = 64))

        val result = harness.createSituation(
            harness.draft(
                name = "Flood",
                imagePng = BattleMapPngFixture.pngBytes(32, 16),
            )
        )

        val created = assertIs<CreateBattleMapSituationUseCase.Result.Created>(result)
        assertEquals("map-1", created.situation.battleMapId)
        assertEquals("Flood", created.situation.name)
        assertEquals(true, created.situation.visible)
        assertEquals(0, created.situation.sortIndex)
        assertEquals(1, harness.situations.all().size)
        val tileDir = harness.mapsRoot.resolve("map-1/situations/sit-1/tiles/0")
        assertTrue(tileDir.toFile().isDirectory)
        assertTrue(tileDir.toFile().listFiles()?.isNotEmpty() == true)
    }

    private class Harness {
        val mapsRoot = Files.createTempDirectory("ww-map-situations")
        val battleMaps = FakeBattleMapRepository()
        val situations = FakeBattleMapSituationRepository()
        val fileStore = BattleMapFileStore(mapsRoot.toFile())
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-30T12:00:00Z") }
        private val ids = EntityIdFactory { "sit-1" }
        val createSituation = CreateBattleMapSituationUseCase(
            situations,
            battleMaps,
            fileStore,
            BattleMapTilePyramidFactory(),
            BattleMapSituationImageTransformer(),
            context,
            ids,
            instant,
        )

        suspend fun activateCampaign() {
            context.setActiveWorldId("world-1")
            context.setActiveCampaignId("campaign-1")
        }

        fun sampleMap(width: Int = 64, height: Int = 64): BattleMap {
            val now = Instant.parse("2026-08-30T12:00:00Z")
            return BattleMap(
                id = "map-1",
                campaignId = "campaign-1",
                name = "Cave",
                originalWidth = width,
                originalHeight = height,
                tileSizePx = 256,
                minZoom = 0,
                maxZoom = 0,
                createdAt = now,
                updatedAt = now,
            )
        }

        fun draft(
            battleMapId: String = "map-1",
            name: String = "Flood",
            imagePng: ByteArray = BattleMapPngFixture.pngBytes(64, 64),
        ): BattleMapSituationDraft {
            return BattleMapSituationDraft(
                battleMapId = battleMapId,
                name = name,
                imagePng = imagePng,
            )
        }
    }
}

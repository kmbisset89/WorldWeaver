package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class CreateBattleMapUseCaseTest {
    @Test
    fun createRequiresActiveCampaign() = runTest {
        val harness = Harness()

        val result = harness.createBattleMap(harness.draft())

        assertIs<CreateBattleMapUseCase.Result.NoActiveCampaign>(result)
        assertTrue(harness.battleMaps.all().isEmpty())
    }

    @Test
    fun createRejectsBlankName() = runTest {
        val harness = Harness()
        harness.activateCampaign()

        val result = harness.createBattleMap(harness.draft(name = "  "))

        assertIs<CreateBattleMapUseCase.Result.InvalidName>(result)
    }

    @Test
    fun createRejectsUnreadableImage() = runTest {
        val harness = Harness()
        harness.activateCampaign()

        val result = harness.createBattleMap(harness.draft(imagePng = byteArrayOf(1, 2, 3)))

        assertIs<CreateBattleMapUseCase.Result.InvalidImage>(result)
    }

    @Test
    fun createRejectsGridLargerThanImage() = runTest {
        val harness = Harness()
        harness.activateCampaign()

        val result = harness.createBattleMap(
            harness.draft(
                imagePng = BattleMapPngFixture.pngBytes(64, 64),
                columns = 80,
                rows = 20,
            )
        )

        assertIs<CreateBattleMapUseCase.Result.InvalidGrid>(result)
    }

    @Test
    fun createPersistsMetadataGridAndTileFiles() = runTest {
        val harness = Harness()
        harness.activateCampaign()

        val result = harness.createBattleMap(
            harness.draft(
                imagePng = BattleMapPngFixture.pngBytes(512, 512),
                columns = 16,
                rows = 12,
                unitName = "sq",
                unitsPerTile = 2.5,
            )
        )

        val created = assertIs<CreateBattleMapUseCase.Result.Created>(result)
        assertEquals("campaign-1", created.battleMap.campaignId)
        assertEquals("Cave", created.battleMap.name)
        assertEquals(512, created.battleMap.originalWidth)
        assertEquals(16, created.battleMap.columns)
        assertEquals(12, created.battleMap.rows)
        assertEquals("sq", created.battleMap.unitName)
        assertEquals(2.5, created.battleMap.unitsPerTile)
        assertEquals(1, harness.battleMaps.all().size)
        val tileDir = harness.mapsRoot.resolve(created.battleMap.id).resolve("tiles/0")
        assertTrue(tileDir.toFile().isDirectory)
        assertTrue(tileDir.toFile().listFiles()?.isNotEmpty() == true)
    }

    private class Harness {
        val mapsRoot = Files.createTempDirectory("ww-maps")
        val battleMaps = FakeBattleMapRepository()
        val fileStore = BattleMapFileStore(mapsRoot.toFile())
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        private val ids = EntityIdFactory { "map-1" }
        val createBattleMap = CreateBattleMapUseCase(
            battleMaps,
            fileStore,
            BattleMapTilePyramidFactory(),
            context,
            ids,
            instant,
        )

        suspend fun activateCampaign() {
            context.setActiveWorldId("world-1")
            context.setActiveCampaignId("campaign-1")
        }

        fun draft(
            name: String = "Cave",
            imagePng: ByteArray = BattleMapPngFixture.pngBytes(64, 64),
            columns: Int = 8,
            rows: Int = 8,
            unitName: String = "ft",
            unitsPerTile: Double = 5.0,
        ): BattleMapDraft {
            return BattleMapDraft(
                name = name,
                imagePng = imagePng,
                columns = columns,
                rows = rows,
                unitName = unitName,
                unitsPerTile = unitsPerTile,
            )
        }
    }
}

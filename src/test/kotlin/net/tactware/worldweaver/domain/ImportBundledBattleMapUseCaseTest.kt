package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class ImportBundledBattleMapUseCaseTest {
    @Test
    fun importRequiresActiveCampaign() = runTest {
        val harness = Harness()

        val result = harness.importBundled("greenwood-clearing")

        assertIs<ImportBundledBattleMapUseCase.Result.NoActiveCampaign>(result)
        assertTrue(harness.battleMaps.all().isEmpty())
    }

    @Test
    fun importRejectsUnknownEntry() = runTest {
        val harness = Harness()
        harness.activateCampaign()

        val result = harness.importBundled("not-a-map")

        assertIs<ImportBundledBattleMapUseCase.Result.UnknownEntry>(result)
    }

    @Test
    fun importRejectsMissingAsset() = runTest {
        val harness = Harness()
        harness.activateCampaign()

        val result = harness.importBundled("greenwood-clearing")

        assertIs<ImportBundledBattleMapUseCase.Result.MissingAsset>(result)
    }

    @Test
    fun importRejectsDuplicateName() = runTest {
        val harness = Harness()
        harness.activateCampaign()
        harness.writePng("06_greenwood_clearing.png")
        harness.battleMaps.insert(harness.existingMap("Greenwood Clearing"))

        val result = harness.importBundled("greenwood-clearing")

        assertIs<ImportBundledBattleMapUseCase.Result.AlreadyPresent>(result)
        assertEquals(1, harness.battleMaps.all().size)
    }

    @Test
    fun importCreatesStandaloneMapOnTwentyByTwentyGrid() = runTest {
        val harness = Harness()
        harness.activateCampaign()
        harness.writePng("06_greenwood_clearing.png")

        val result = harness.importBundled("greenwood-clearing")

        val imported = assertIs<ImportBundledBattleMapUseCase.Result.Imported>(result)
        assertEquals("Greenwood Clearing", imported.battleMap.name)
        assertEquals(20, imported.battleMap.columns)
        assertEquals(20, imported.battleMap.rows)
        assertEquals("ft", imported.battleMap.unitName)
        assertEquals(5.0, imported.battleMap.unitsPerTile)
        assertTrue(harness.situations.all().isEmpty())
    }

    @Test
    fun importCreatesMediumMapOnThirtyByThirtyGrid() = runTest {
        val harness = Harness()
        harness.activateCampaign()
        harness.writePng("06_riverford_village.png")

        val result = harness.importBundled("riverford-village")

        val imported = assertIs<ImportBundledBattleMapUseCase.Result.Imported>(result)
        assertEquals("Riverford Village", imported.battleMap.name)
        assertEquals(30, imported.battleMap.columns)
        assertEquals(30, imported.battleMap.rows)
        assertEquals("ft", imported.battleMap.unitName)
        assertEquals(5.0, imported.battleMap.unitsPerTile)
        assertTrue(harness.situations.all().isEmpty())
    }

    @Test
    fun importAddsHiddenSituationStages() = runTest {
        val harness = Harness()
        harness.activateCampaign()
        harness.writePng("01_whispering_shrine_A_calm.png")
        harness.writePng("01_whispering_shrine_B_falling_trees.png")
        harness.writePng("01_whispering_shrine_C_wildfire.png")

        val result = harness.importBundled("whispering-shrine")

        val imported = assertIs<ImportBundledBattleMapUseCase.Result.Imported>(result)
        assertEquals("Whispering Shrine", imported.battleMap.name)
        assertEquals(listOf("Falling trees", "Wildfire"), harness.situations.all().map { it.name })
        assertTrue(harness.situations.all().all { !it.visible })
    }

    @Test
    fun importAddsHiddenSituationStagesForMediumMaps() = runTest {
        val harness = Harness()
        harness.activateCampaign()
        harness.writePng("01_stormwatch_keep_A_holding.png")
        harness.writePng("01_stormwatch_keep_B_gate_breached.png")
        harness.writePng("01_stormwatch_keep_C_inner_bailey_burning.png")

        val result = harness.importBundled("stormwatch-keep")

        val imported = assertIs<ImportBundledBattleMapUseCase.Result.Imported>(result)
        assertEquals("Stormwatch Keep", imported.battleMap.name)
        assertEquals(30, imported.battleMap.columns)
        assertEquals(30, imported.battleMap.rows)
        assertEquals(
            listOf("Gate breached", "Inner bailey burning"),
            harness.situations.all().map { it.name },
        )
        assertTrue(harness.situations.all().all { !it.visible })
    }

    private class Harness {
        val mapsRoot = Files.createTempDirectory("ww-import-bundled-maps")
        val assetsRoot = Files.createTempDirectory("ww-bundled-assets")
        val battleMaps = FakeBattleMapRepository()
        val situations = FakeBattleMapSituationRepository()
        val context = FakeActiveContextRepository()
        private var nextId = 0
        private val ids = EntityIdFactory { "id-${++nextId}" }
        private val instant = InstantProvider { Instant.parse("2026-09-02T12:00:00Z") }
        private val fileStore = BattleMapFileStore(mapsRoot.toFile())
        private val pyramidFactory = BattleMapTilePyramidFactory()
        val importBundled = ImportBundledBattleMapUseCase(
            catalogLoader = BundledBattleMapCatalogLoader(roots = listOf(assetsRoot.toFile())),
            createBattleMap = CreateBattleMapUseCase(
                battleMaps,
                fileStore,
                pyramidFactory,
                context,
                ids,
                instant,
            ),
            createSituation = CreateBattleMapSituationUseCase(
                situations,
                battleMaps,
                fileStore,
                pyramidFactory,
                BattleMapSituationImageTransformer(),
                context,
                ids,
                instant,
            ),
            battleMapRepository = battleMaps,
            activeContextRepository = context,
        )

        suspend fun activateCampaign() {
            context.setActiveWorldId("world-1")
            context.setActiveCampaignId("campaign-1")
        }

        fun writePng(fileName: String) {
            Files.write(assetsRoot.resolve(fileName), BattleMapPngFixture.pngBytes(64, 64))
        }

        fun existingMap(name: String): BattleMap {
            val now = Instant.parse("2026-09-02T12:00:00Z")
            return BattleMap(
                id = "existing",
                campaignId = "campaign-1",
                name = name,
                originalWidth = 64,
                originalHeight = 64,
                tileSizePx = 256,
                minZoom = 0,
                maxZoom = 0,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}

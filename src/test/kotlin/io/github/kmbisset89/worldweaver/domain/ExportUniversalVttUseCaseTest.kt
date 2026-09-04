package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.Files
import java.time.Instant
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class ExportUniversalVttUseCaseTest {
    @Test
    fun exportWritesUvttWithImageAndGrid() = runTest {
        val harness = Harness()
        val png = BattleMapPngFixture.pngBytes(512, 512)
        harness.fileStore.write("map-1", MapTilePyramidFactory().create(png)!!)
        harness.battleMaps.insert(harness.sampleMap(width = 512, height = 512, columns = 16, rows = 16))
        val dest = harness.tempDir.resolve("clockwork.uvtt").toFile()

        val result = harness.export("map-1", dest)

        assertIs<ExportUniversalVttUseCase.Result.Written>(result)
        val parsed = Json.parseToJsonElement(dest.readText()).jsonObject
        assertEquals(0.2, parsed.getValue("format").jsonPrimitive.double)
        val resolution = parsed.getValue("resolution").jsonObject
        assertEquals(16.0, resolution.getValue("map_size").jsonObject.getValue("x").jsonPrimitive.double)
        assertEquals(32, resolution.getValue("pixels_per_grid").jsonPrimitive.int)
        assertTrue(parsed.getValue("line_of_sight").jsonArray.isEmpty())
        assertTrue(parsed.getValue("lights").jsonArray.isEmpty())
        val encoded = parsed.getValue("image").jsonPrimitive.content
        assertEquals(png.toList(), Base64.getDecoder().decode(encoded).toList())
    }

    @Test
    fun exportMissingMapReturnsNotFound() = runTest {
        val harness = Harness()
        val dest = harness.tempDir.resolve("missing.uvtt").toFile()

        val result = harness.export("missing", dest)

        assertIs<ExportUniversalVttUseCase.Result.MapNotFound>(result)
        assertTrue(!dest.exists())
    }

    @Test
    fun exportMapWithoutImageReturnsImageMissing() = runTest {
        val harness = Harness()
        harness.battleMaps.insert(harness.sampleMap())
        val dest = harness.tempDir.resolve("empty.uvtt").toFile()

        val result = harness.export("map-1", dest)

        assertIs<ExportUniversalVttUseCase.Result.ImageMissing>(result)
        assertTrue(!dest.exists())
    }

    private class Harness {
        val tempDir = Files.createTempDirectory("ww-uvtt")
        val battleMaps = FakeBattleMapRepository()
        val fileStore = BattleMapFileStore(tempDir.resolve("maps").toFile())
        val exportUniversalVtt = ExportUniversalVttUseCase(
            battleMaps,
            fileStore,
            UniversalVttDocumentFactory(),
        )

        suspend fun export(battleMapId: String, dest: java.io.File): ExportUniversalVttUseCase.Result {
            return exportUniversalVtt(battleMapId, dest)
        }

        fun sampleMap(
            width: Int = 64,
            height: Int = 64,
            columns: Int = 8,
            rows: Int = 8,
        ): BattleMap {
            val now = Instant.parse("2026-09-04T12:00:00Z")
            return BattleMap(
                id = "map-1",
                campaignId = "campaign-1",
                name = "Cave",
                originalWidth = width,
                originalHeight = height,
                tileSizePx = 256,
                minZoom = 0,
                maxZoom = 0,
                columns = columns,
                rows = rows,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}

package io.github.kmbisset89.worldweaver.domain

import java.time.Instant
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class UniversalVttDocumentFactoryTest {
    @Test
    fun createEmbedsPngAndGridWithoutWallsOrLights() {
        val png = BattleMapPngFixture.pngBytes(640, 480)
        val document = UniversalVttDocumentFactory().create(
            battleMap = sampleMap(width = 640, height = 480, columns = 20, rows = 15),
            originalPng = png,
        )

        assertEquals(0.2, document.format)
        assertEquals(0.0, document.resolution.mapOrigin.x)
        assertEquals(0.0, document.resolution.mapOrigin.y)
        assertEquals(20.0, document.resolution.mapSize.x)
        assertEquals(15.0, document.resolution.mapSize.y)
        assertEquals(32, document.resolution.pixelsPerGrid)
        assertTrue(document.lineOfSight.isEmpty())
        assertTrue(document.objectsLineOfSight.isEmpty())
        assertTrue(document.portals.isEmpty())
        assertTrue(document.lights.isEmpty())
        assertTrue(document.environment.bakedLighting)
        assertEquals("ffffffff", document.environment.ambientLight)
        assertEquals(png.toList(), Base64.getDecoder().decode(document.image).toList())
    }

    private fun sampleMap(
        width: Int,
        height: Int,
        columns: Int,
        rows: Int,
    ): BattleMap {
        val now = Instant.parse("2026-09-04T12:00:00Z")
        return BattleMap(
            id = "map-1",
            campaignId = "campaign-1",
            name = "Clockwork Foundry",
            originalWidth = width,
            originalHeight = height,
            tileSizePx = 256,
            minZoom = 0,
            maxZoom = 0,
            columns = columns,
            rows = rows,
            unitName = "ft",
            unitsPerTile = 5.0,
            createdAt = now,
            updatedAt = now,
        )
    }
}

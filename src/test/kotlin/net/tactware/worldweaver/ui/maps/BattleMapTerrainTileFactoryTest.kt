package net.tactware.worldweaver.ui.maps

import net.tactware.worldweaver.domain.BattleMap
import net.tactware.worldweaver.domain.GridCell
import java.awt.Color
import java.io.ByteArrayInputStream
import java.time.Instant
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class BattleMapTerrainTileFactoryTest {
    private val factory = BattleMapTerrainTileFactory()

    @Test
    fun emptyTerrainReturnsNull() {
        assertNull(factory.tilePng(map(), dbZoom = 0, col = 0, row = 0))
    }

    @Test
    fun difficultCellPaintsAmberOnThatTile() {
        val bytes = factory.tilePng(
            map(difficult = setOf(GridCell(0, 0))),
            dbZoom = 0,
            col = 0,
            row = 0,
        )
        assertNotNull(bytes)
        val pixel = Color(ImageIO.read(ByteArrayInputStream(bytes)).getRGB(10, 10), true)
        assertTrue(pixel.alpha > 0)
        assertTrue(pixel.red > pixel.blue)
    }

    @Test
    fun blockedCellPaintsDarkerThanDifficult() {
        val difficult = factory.tilePng(
            map(difficult = setOf(GridCell(0, 0))),
            dbZoom = 0,
            col = 0,
            row = 0,
        )
        val blocked = factory.tilePng(
            map(blocked = setOf(GridCell(0, 0))),
            dbZoom = 0,
            col = 0,
            row = 0,
        )
        assertNotNull(difficult)
        assertNotNull(blocked)
        val difficultPixel = Color(ImageIO.read(ByteArrayInputStream(difficult)).getRGB(10, 10), true)
        val blockedPixel = Color(ImageIO.read(ByteArrayInputStream(blocked)).getRGB(10, 10), true)
        assertTrue(blockedPixel.alpha > difficultPixel.alpha)
    }

    @Test
    fun unmarkedTileStaysEmpty() {
        assertNull(
            factory.tilePng(
                map(difficult = setOf(GridCell(1, 0))),
                dbZoom = 0,
                col = 0,
                row = 0,
            ),
        )
    }

    private fun map(
        blocked: Set<GridCell> = emptySet(),
        difficult: Set<GridCell> = emptySet(),
    ): BattleMap {
        return BattleMap(
            id = "map-1",
            campaignId = "campaign-1",
            name = "Cave",
            originalWidth = 512,
            originalHeight = 512,
            tileSizePx = 256,
            minZoom = 0,
            maxZoom = 0,
            columns = 2,
            rows = 2,
            blockedCells = blocked,
            difficultCells = difficult,
            createdAt = Instant.parse("2026-08-30T12:00:00Z"),
            updatedAt = Instant.parse("2026-08-30T12:00:00Z"),
        )
    }
}

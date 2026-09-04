package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class MapTilePyramidFactoryTest {
    private val factory = MapTilePyramidFactory()

    @Test
    fun smallImageProducesSingleZoomLevel() {
        val pyramid = factory.create(BattleMapPngFixture.pngBytes(512, 512))

        assertNotNull(pyramid)
        assertEquals(512, pyramid.originalWidth)
        assertEquals(512, pyramid.originalHeight)
        assertEquals(256, pyramid.tileSizePx)
        assertEquals(0, pyramid.minZoom)
        assertEquals(0, pyramid.maxZoom)
        assertEquals(4, pyramid.tiles.size)
        assertTrue(pyramid.tiles.all { it.zoom == 0 })
    }

    @Test
    fun largerImageAddsACoarserZoomLevel() {
        val pyramid = factory.create(BattleMapPngFixture.pngBytes(1024, 1024))

        assertNotNull(pyramid)
        assertEquals(0, pyramid.minZoom)
        assertEquals(1, pyramid.maxZoom)
        assertTrue(pyramid.tiles.any { it.zoom == 0 })
        assertTrue(pyramid.tiles.any { it.zoom == 1 })
    }

    @Test
    fun unreadableBytesReturnNull() {
        assertNull(factory.create(byteArrayOf(1, 2, 3, 4)))
    }
}

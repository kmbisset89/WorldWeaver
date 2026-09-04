package io.github.kmbisset89.worldweaver.domain

import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.ceil

internal class MapTilePyramidFactory {
    fun create(imagePng: ByteArray): MapTilePyramid? {
        val original = decodePng(imagePng) ?: return null
        if (original.width <= 0 || original.height <= 0) {
            return null
        }
        val source = ensureArgb(original)
        val zoomLevels = computeZoomLevels(source.width, source.height, TILE_SIZE_PX)
        val tiles = ArrayList<MapTile>()
        for (zoom in zoomLevels) {
            val scaled = scaleDownByPow2(source, zoom)
            val tilesX = ceil(scaled.width.toDouble() / TILE_SIZE_PX).toInt().coerceAtLeast(1)
            val tilesY = ceil(scaled.height.toDouble() / TILE_SIZE_PX).toInt().coerceAtLeast(1)
            for (y in 0 until tilesY) {
                for (x in 0 until tilesX) {
                    val px = x * TILE_SIZE_PX
                    val py = y * TILE_SIZE_PX
                    val width = (scaled.width - px).coerceAtMost(TILE_SIZE_PX).coerceAtLeast(0)
                    val height = (scaled.height - py).coerceAtMost(TILE_SIZE_PX).coerceAtLeast(0)
                    if (width == 0 || height == 0) {
                        continue
                    }
                    val tileImage = scaled.getSubimage(px, py, width, height)
                    tiles.add(
                        MapTile(
                            zoom = zoom,
                            x = x,
                            y = y,
                            width = width,
                            height = height,
                            imagePng = toPngBytes(tileImage),
                        )
                    )
                }
            }
        }
        return MapTilePyramid(
            originalWidth = source.width,
            originalHeight = source.height,
            tileSizePx = TILE_SIZE_PX,
            minZoom = zoomLevels.first(),
            maxZoom = zoomLevels.last(),
            originalPng = imagePng,
            tiles = tiles,
        )
    }

    private fun decodePng(imagePng: ByteArray): BufferedImage? {
        return try {
            ImageIO.read(ByteArrayInputStream(imagePng))
        } catch (_: Exception) {
            null
        }
    }

    private fun computeZoomLevels(width: Int, height: Int, tileSizePx: Int): List<Int> {
        var zoom = 0
        var currentWidth = width
        var currentHeight = height
        while (true) {
            val nextWidth = ((currentWidth + 1) / 2).coerceAtLeast(1)
            val nextHeight = ((currentHeight + 1) / 2).coerceAtLeast(1)
            if (nextWidth <= tileSizePx && nextHeight <= tileSizePx) {
                break
            }
            if (zoom >= MAX_ZOOM_LEVEL) {
                break
            }
            zoom++
            currentWidth = nextWidth
            currentHeight = nextHeight
        }
        return (0..zoom).toList()
    }

    private fun scaleDownByPow2(source: BufferedImage, zoom: Int): BufferedImage {
        if (zoom <= 0) {
            return source
        }
        val factor = 1 shl zoom
        val targetWidth = ((source.width + factor - 1) / factor).coerceAtLeast(1)
        val targetHeight = ((source.height + factor - 1) / factor).coerceAtLeast(1)
        return scaleBufferedImage(source, targetWidth, targetHeight)
    }

    private fun ensureArgb(source: BufferedImage): BufferedImage {
        if (source.type == BufferedImage.TYPE_INT_ARGB) {
            return source
        }
        val out = BufferedImage(source.width, source.height, BufferedImage.TYPE_INT_ARGB)
        val graphics = out.createGraphics()
        graphics.drawImage(source, 0, 0, null)
        graphics.dispose()
        return out
    }

    private fun scaleBufferedImage(
        source: BufferedImage,
        targetWidth: Int,
        targetHeight: Int,
    ): BufferedImage {
        val out = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = out.createGraphics()
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null)
        graphics.dispose()
        return out
    }

    private fun toPngBytes(image: BufferedImage): ByteArray {
        val output = ByteArrayOutputStream()
        ImageIO.write(image, "png", output)
        return output.toByteArray()
    }

    private companion object {
        const val TILE_SIZE_PX = 256
        const val MAX_ZOOM_LEVEL = 10
    }
}

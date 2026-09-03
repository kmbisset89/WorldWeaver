package io.github.kmbisset89.worldweaver.ui.maps

import io.github.kmbisset89.worldweaver.domain.BattleMap
import io.github.kmbisset89.worldweaver.domain.BattleMapGridGeometry
import io.github.kmbisset89.worldweaver.domain.GridCell
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

internal class BattleMapFogTileFactory {
    fun tilePng(
        battleMap: BattleMap,
        dbZoom: Int,
        col: Int,
        row: Int,
        opaque: Boolean,
    ): ByteArray? {
        if (!battleMap.fogEnabled) {
            return null
        }
        val factor = 1 shl dbZoom.coerceAtLeast(0)
        val scaledWidth = ((battleMap.originalWidth + factor - 1) / factor).coerceAtLeast(1)
        val scaledHeight = ((battleMap.originalHeight + factor - 1) / factor).coerceAtLeast(1)
        val tileX = col * battleMap.tileSizePx
        val tileY = row * battleMap.tileSizePx
        val tileWidth = (scaledWidth - tileX).coerceAtMost(battleMap.tileSizePx).coerceAtLeast(0)
        val tileHeight = (scaledHeight - tileY).coerceAtMost(battleMap.tileSizePx).coerceAtLeast(0)
        if (tileWidth == 0 || tileHeight == 0) {
            return null
        }
        val geometry = BattleMapGridGeometry(
            imageWidth = battleMap.originalWidth,
            imageHeight = battleMap.originalHeight,
            columns = battleMap.columns,
            rows = battleMap.rows,
        )
        val image = BufferedImage(tileWidth, tileHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        val fill = if (opaque) {
            Color(0, 0, 0, 255)
        } else {
            Color(0, 0, 0, 150)
        }
        graphics.color = fill
        var painted = false
        for (column in 0 until battleMap.columns) {
            for (gridRow in 0 until battleMap.rows) {
                val cell = GridCell(column, gridRow)
                if (battleMap.isRevealedToPlayers(cell)) {
                    continue
                }
                val rect = geometry.pixelRect(cell) ?: continue
                val destX = (rect.x / factor) - tileX
                val destY = (rect.y / factor) - tileY
                val destWidth = ((rect.x + rect.width + factor - 1) / factor) - (rect.x / factor)
                val destHeight = ((rect.y + rect.height + factor - 1) / factor) - (rect.y / factor)
                val clippedX = destX.coerceAtLeast(0)
                val clippedY = destY.coerceAtLeast(0)
                val clippedW = (destX + destWidth).coerceAtMost(tileWidth) - clippedX
                val clippedH = (destY + destHeight).coerceAtMost(tileHeight) - clippedY
                if (clippedW > 0 && clippedH > 0) {
                    graphics.fillRect(clippedX, clippedY, clippedW, clippedH)
                    painted = true
                }
            }
        }
        graphics.dispose()
        if (!painted) {
            return null
        }
        val output = ByteArrayOutputStream()
        ImageIO.write(image, "png", output)
        return output.toByteArray()
    }
}

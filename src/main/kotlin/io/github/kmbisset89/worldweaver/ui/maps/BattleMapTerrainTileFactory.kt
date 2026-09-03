package io.github.kmbisset89.worldweaver.ui.maps

import io.github.kmbisset89.worldweaver.domain.BattleMap
import io.github.kmbisset89.worldweaver.domain.BattleMapGridGeometry
import io.github.kmbisset89.worldweaver.domain.GridCell
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

internal class BattleMapTerrainTileFactory {
    fun tilePng(
        battleMap: BattleMap,
        dbZoom: Int,
        col: Int,
        row: Int,
    ): ByteArray? {
        if (battleMap.blockedCells.isEmpty() && battleMap.difficultCells.isEmpty()) {
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
        var painted = false
        for (cell in battleMap.blockedCells + battleMap.difficultCells) {
            val fill = if (cell in battleMap.blockedCells) {
                BlockedFill
            } else {
                DifficultFill
            }
            if (paintCell(graphics, geometry, cell, factor, tileX, tileY, tileWidth, tileHeight, fill)) {
                painted = true
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

    private fun paintCell(
        graphics: Graphics2D,
        geometry: BattleMapGridGeometry,
        cell: GridCell,
        factor: Int,
        tileX: Int,
        tileY: Int,
        tileWidth: Int,
        tileHeight: Int,
        fill: Color,
    ): Boolean {
        val rect = geometry.pixelRect(cell) ?: return false
        val destX = (rect.x / factor) - tileX
        val destY = (rect.y / factor) - tileY
        val destWidth = ((rect.x + rect.width + factor - 1) / factor) - (rect.x / factor)
        val destHeight = ((rect.y + rect.height + factor - 1) / factor) - (rect.y / factor)
        val clippedX = destX.coerceAtLeast(0)
        val clippedY = destY.coerceAtLeast(0)
        val clippedW = (destX + destWidth).coerceAtMost(tileWidth) - clippedX
        val clippedH = (destY + destHeight).coerceAtMost(tileHeight) - clippedY
        if (clippedW <= 0 || clippedH <= 0) {
            return false
        }
        graphics.color = fill
        graphics.fillRect(clippedX, clippedY, clippedW, clippedH)
        if (fill == DifficultFill) {
            graphics.color = DifficultHatch
            val previousClip = graphics.clip
            graphics.clipRect(clippedX, clippedY, clippedW, clippedH)
            val spacing = (12 / factor).coerceAtLeast(4)
            var line = clippedX - clippedH
            while (line < clippedX + clippedW) {
                graphics.drawLine(line, clippedY + clippedH, line + clippedH, clippedY)
                line += spacing
            }
            graphics.clip = previousClip
        }
        return true
    }

    private companion object {
        val BlockedFill = Color(90, 20, 20, 150)
        val DifficultFill = Color(200, 130, 20, 70)
        val DifficultHatch = Color(180, 90, 10, 170)
    }
}

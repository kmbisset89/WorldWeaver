package net.tactware.worldweaver.domain

import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.roundToInt

internal class BattleMapImageScaler {
    fun scale(imagePng: ByteArray, percent: Int): ByteArray? {
        val source = decode(imagePng) ?: return null
        val clamped = percent.coerceIn(MIN_PERCENT, MAX_PERCENT)
        if (clamped == 100) {
            return imagePng
        }
        val targetWidth = (source.width * clamped / 100.0).roundToInt().coerceAtLeast(1)
        val targetHeight = (source.height * clamped / 100.0).roundToInt().coerceAtLeast(1)
        val scaled = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = scaled.createGraphics()
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null)
        graphics.dispose()
        val output = ByteArrayOutputStream()
        ImageIO.write(scaled, "png", output)
        return output.toByteArray()
    }

    private fun decode(imagePng: ByteArray): BufferedImage? {
        return try {
            ImageIO.read(ByteArrayInputStream(imagePng))
        } catch (_: Exception) {
            null
        }
    }

    private companion object {
        const val MIN_PERCENT = 10
        const val MAX_PERCENT = 400
    }
}

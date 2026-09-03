package io.github.kmbisset89.worldweaver.domain

import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

internal class BattleMapSituationImageTransformer {
    fun transform(imagePng: ByteArray, targetWidth: Int, targetHeight: Int): ByteArray? {
        if (targetWidth < 1 || targetHeight < 1) {
            return null
        }
        val source = decode(imagePng) ?: return null
        if (source.width <= 0 || source.height <= 0) {
            return null
        }
        if (source.width == targetWidth && source.height == targetHeight) {
            return imagePng
        }
        val fitted = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = fitted.createGraphics()
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null)
        graphics.dispose()
        val output = ByteArrayOutputStream()
        ImageIO.write(fitted, "png", output)
        return output.toByteArray()
    }

    private fun decode(imagePng: ByteArray): BufferedImage? {
        return try {
            ImageIO.read(ByteArrayInputStream(imagePng))
        } catch (_: Exception) {
            null
        }
    }
}

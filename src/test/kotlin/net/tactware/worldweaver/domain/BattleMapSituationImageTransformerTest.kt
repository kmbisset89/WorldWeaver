package net.tactware.worldweaver.domain

import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class BattleMapSituationImageTransformerTest {
    private val transformer = BattleMapSituationImageTransformer()

    @Test
    fun sameSizeKeepsOriginalBytes() {
        val original = BattleMapPngFixture.pngBytes(64, 64)

        val fitted = transformer.transform(original, 64, 64)

        assertNotNull(fitted)
        assertEquals(original.toList(), fitted.toList())
    }

    @Test
    fun differentSizeScalesToTarget() {
        val fitted = transformer.transform(BattleMapPngFixture.pngBytes(64, 32), 128, 96)

        assertNotNull(fitted)
        val image = ImageIO.read(ByteArrayInputStream(fitted))
        assertNotNull(image)
        assertEquals(128, image.width)
        assertEquals(96, image.height)
    }

    @Test
    fun unreadableBytesReturnNull() {
        assertNull(transformer.transform(byteArrayOf(1, 2, 3), 64, 64))
    }
}

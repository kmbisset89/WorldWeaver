package net.tactware.worldweaver.domain

import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class BattleMapImageScalerTest {
    private val scaler = BattleMapImageScaler()

    @Test
    fun fiftyPercentHalvesAFixtureImage() {
        val scaled = scaler.scale(BattleMapPngFixture.pngBytes(512, 256), 50)

        assertNotNull(scaled)
        val image = ImageIO.read(ByteArrayInputStream(scaled))
        assertNotNull(image)
        assertEquals(256, image.width)
        assertEquals(128, image.height)
    }

    @Test
    fun oneHundredPercentKeepsOriginalBytes() {
        val original = BattleMapPngFixture.pngBytes(64, 64)

        val scaled = scaler.scale(original, 100)

        assertNotNull(scaled)
        assertEquals(original.toList(), scaled.toList())
    }

    @Test
    fun unreadableBytesReturnNull() {
        assertNull(scaler.scale(byteArrayOf(1, 2, 3), 50))
    }
}

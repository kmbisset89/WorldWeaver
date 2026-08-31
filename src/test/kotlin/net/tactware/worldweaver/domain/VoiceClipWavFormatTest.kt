package net.tactware.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class VoiceClipWavFormatTest {
    @Test
    fun wrapPcmProducesValidWave() {
        val wav = VoiceClipWavFormat.wrapPcm(ByteArray(200))
        assertTrue(VoiceClipWavFormat.isValid(wav))
    }

    @Test
    fun rejectsTruncatedAndNonWaveBytes() {
        assertFalse(VoiceClipWavFormat.isValid(byteArrayOf(1, 2, 3)))
        assertFalse(VoiceClipWavFormat.isValid("RIFF".toByteArray() + ByteArray(40)))
        val wav = VoiceClipWavFormat.wrapPcm(ByteArray(20))
        assertFalse(VoiceClipWavFormat.isValid(wav.copyOf(wav.size - 4)))
    }
}

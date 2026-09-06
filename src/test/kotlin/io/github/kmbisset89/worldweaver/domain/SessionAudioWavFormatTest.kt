package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SessionAudioWavFormatTest {
    @Test
    fun headerProducesAValidWavWhenPcmIsPresent() {
        val header = SessionAudioWavFormat.header(200)
        val wav = header + ByteArray(200)
        assertTrue(SessionAudioWavFormat.isValid(wav))
    }

    @Test
    fun rejectsTruncatedBytes() {
        assertFalse(SessionAudioWavFormat.isValid(byteArrayOf(1, 2, 3)))
        val header = SessionAudioWavFormat.header(20)
        assertFalse(SessionAudioWavFormat.isValid(header.copyOf(header.size - 4)))
    }
}

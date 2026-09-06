package io.github.kmbisset89.worldweaver.domain

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue

internal class SessionAudioWavWriterTest {
    @Test
    fun closeWritesAValidWavHeader() {
        val file = Files.createTempFile("ww-session", ".wav").toFile()
        SessionAudioWavWriter(file).use { writer ->
            writer.writePcm(ByteArray(200), 200)
        }
        assertTrue(SessionAudioWavFormat.isValid(file.readBytes()))
    }
}

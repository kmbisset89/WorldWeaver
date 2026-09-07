package io.github.kmbisset89.worldweaver.domain

import java.io.File
import java.nio.file.Files
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class AtmosphereMusicPlayerTest {
    private val tempDir = Files.createTempDirectory("ww-atmosphere-music-player").toFile()
    private val player = AtmosphereMusicPlayer()

    @AfterTest
    fun tearDown() {
        player.stop()
        tempDir.deleteRecursively()
    }

    @Test
    fun missingFileFails() {
        val result = player.play(
            path = File(tempDir, "missing.mp3").absolutePath,
            loop = false,
            volume = 50,
            onFinished = {},
        )
        val failed = assertIs<AtmosphereMusicPlayer.Result.Failed>(result)
        assertEquals("That music file is missing", failed.message)
    }

    @Test
    fun playsAndStopsAWavFile() {
        val wav = File(tempDir, "tavern.wav")
        wav.writeBytes(VoiceClipWavFormat.wrapPcm(ByteArray(VoiceClipWavFormat.SAMPLE_RATE)))
        val format = AudioFormat(
            VoiceClipWavFormat.SAMPLE_RATE.toFloat(),
            VoiceClipWavFormat.BITS_PER_SAMPLE,
            VoiceClipWavFormat.CHANNELS,
            true,
            false,
        )
        if (!AudioSystem.isLineSupported(DataLine.Info(SourceDataLine::class.java, format))) {
            return
        }
        val result = player.play(
            path = wav.absolutePath,
            loop = false,
            volume = 40,
            onFinished = {},
        )
        assertIs<AtmosphereMusicPlayer.Result.Started>(result)
        player.stop()
    }

    @Test
    fun unreadableFileFails() {
        val file = File(tempDir, "broken.mp3")
        file.writeBytes(byteArrayOf())
        val result = player.play(
            path = file.absolutePath,
            loop = false,
            volume = 50,
            onFinished = {},
        )
        val failed = assertIs<AtmosphereMusicPlayer.Result.Failed>(result)
        assertEquals("Could not play that music file", failed.message)
    }
}

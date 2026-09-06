package io.github.kmbisset89.worldweaver.domain

import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class SessionRecordingCaptureServiceTest {
    @Test
    fun audioStartStopWritesWav() {
        val store = SessionRecordingFileStore(Files.createTempDirectory("ww-recordings").toFile())
        val capture = SessionRecordingCaptureService(
            fileStore = store,
            instantProvider = InstantProvider { Instant.parse("2026-09-06T15:00:00Z") },
            microphoneFactory = { _ -> FakeSessionMicrophoneLine() },
            cameraFactory = { null },
            videoEncoderFactory = { _, _, _, _ -> null },
        )

        assertTrue(capture.start("session-1", SessionRecordingKind.Audio, microphoneDeviceId = "mic-1"))
        Thread.sleep(50)
        val recording = capture.stop()

        assertNotNull(recording)
        assertEquals(SessionRecordingKind.Audio, recording.kind)
        assertTrue(SessionAudioWavFormat.isValid(File(recording.path).readBytes()))
    }

    @Test
    fun videoStartStopUsesEncoderAndKeepsPreviewWhenRequested() {
        val store = SessionRecordingFileStore(Files.createTempDirectory("ww-recordings").toFile())
        val encoder = FakeSessionVideoEncoder()
        val camera = FakeSessionCameraFrameSource()
        val capture = SessionRecordingCaptureService(
            fileStore = store,
            instantProvider = InstantProvider { Instant.parse("2026-09-06T15:01:00Z") },
            microphoneFactory = { _ -> FakeSessionMicrophoneLine() },
            cameraFactory = { camera },
            videoEncoderFactory = { file, _, _, _ -> encoder.also { it.file = file } },
        )

        assertTrue(capture.startPreview())
        assertTrue(capture.start("session-1", SessionRecordingKind.Video, microphoneDeviceId = null))
        Thread.sleep(50)
        val recording = capture.stop()

        assertNotNull(recording)
        assertEquals(SessionRecordingKind.Video, recording.kind)
        assertTrue(encoder.stopped)
        assertTrue(File(recording.path).isFile)
    }
}

private class FakeSessionMicrophoneLine : SessionMicrophoneLine {
    override fun read(buffer: ByteArray): Int {
        buffer.fill(1)
        Thread.sleep(10)
        return buffer.size
    }

    override fun close() = Unit
}

private class FakeSessionCameraFrameSource : SessionCameraFrameSource {
    override fun grabImage(): BufferedImage {
        Thread.sleep(10)
        return BufferedImage(4, 4, BufferedImage.TYPE_3BYTE_BGR)
    }

    override fun stop() = Unit
}

private class FakeSessionVideoEncoder : SessionVideoEncoder {
    var file: File? = null
    var stopped = false

    override fun recordImage(image: BufferedImage) {
        file?.writeBytes(ByteArray(32))
    }

    override fun recordPcm(pcm: ByteArray, length: Int) = Unit

    override fun stop() {
        stopped = true
        val dest = file ?: return
        if (!dest.isFile) {
            dest.writeBytes(ByteArray(32))
        }
    }
}

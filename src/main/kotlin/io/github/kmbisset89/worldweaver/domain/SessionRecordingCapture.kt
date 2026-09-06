package io.github.kmbisset89.worldweaver.domain

import java.awt.image.BufferedImage
import kotlinx.coroutines.flow.StateFlow

/**
 * Owns the Tonight session microphone and camera capture lifecycle.
 */
internal interface SessionRecordingCapture {
    val previewImages: StateFlow<BufferedImage?>
    val isRecording: Boolean
    fun startPreview(): Boolean
    fun stopPreview()
    fun start(sessionId: String, kind: SessionRecordingKind, microphoneDeviceId: String?): Boolean
    fun stop(): SessionRecording?
    fun shutdown()
}

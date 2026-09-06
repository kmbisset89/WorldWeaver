package io.github.kmbisset89.worldweaver.domain

import java.awt.image.BufferedImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class FakeSessionRecordingCapture : SessionRecordingCapture {
    private val _previewImages = MutableStateFlow<BufferedImage?>(null)
    override val previewImages: StateFlow<BufferedImage?> = _previewImages.asStateFlow()
    override var isRecording: Boolean = false
    var startPreviewCalls: Int = 0
    var stopPreviewCalls: Int = 0
    var shutdownCalls: Int = 0
    var lastStartSessionId: String? = null
    var lastStartKind: SessionRecordingKind? = null
    var startResult: Boolean = true
    var startPreviewResult: Boolean = true
    private val stopped = mutableListOf<SessionRecording>()

    fun enqueueStopResult(recording: SessionRecording) {
        stopped += recording
    }

    override fun startPreview(): Boolean {
        startPreviewCalls += 1
        if (startPreviewResult) {
            _previewImages.value = BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB)
        }
        return startPreviewResult
    }

    override fun stopPreview() {
        stopPreviewCalls += 1
        _previewImages.value = null
    }

    var lastMicrophoneDeviceId: String? = null

    override fun start(
        sessionId: String,
        kind: SessionRecordingKind,
        microphoneDeviceId: String?,
    ): Boolean {
        lastStartSessionId = sessionId
        lastStartKind = kind
        lastMicrophoneDeviceId = microphoneDeviceId
        if (!startResult) {
            return false
        }
        isRecording = true
        return true
    }

    override fun stop(): SessionRecording? {
        if (!isRecording && stopped.isEmpty()) {
            isRecording = false
            return null
        }
        isRecording = false
        return if (stopped.isNotEmpty()) stopped.removeAt(0) else null
    }

    override fun shutdown() {
        shutdownCalls += 1
        stop()
        stopPreview()
    }
}

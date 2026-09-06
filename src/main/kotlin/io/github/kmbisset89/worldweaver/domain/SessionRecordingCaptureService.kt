package io.github.kmbisset89.worldweaver.domain

import java.awt.image.BufferedImage
import java.io.File
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class SessionRecordingCaptureService(
    private val fileStore: SessionRecordingFileStore,
    private val instantProvider: InstantProvider,
    private val microphoneFactory: SessionMicrophoneLine.Factory,
    private val cameraFactory: SessionCameraFrameSource.Factory,
    private val videoEncoderFactory: SessionVideoEncoder.Factory,
) : SessionRecordingCapture {
    private val lock = Any()
    private val encoderLock = Any()
    private val _previewImages = MutableStateFlow<BufferedImage?>(null)
    override val previewImages: StateFlow<BufferedImage?> = _previewImages.asStateFlow()

    private var previewRequested = false
    private var camera: SessionCameraFrameSource? = null
    private var previewThread: Thread? = null
    private var microphone: SessionMicrophoneLine? = null
    private var audioThread: Thread? = null
    private var wavWriter: SessionAudioWavWriter? = null
    private var videoEncoder: SessionVideoEncoder? = null
    private var recordingKind: SessionRecordingKind? = null
    private var recordingSessionId: String? = null
    private var recordingFile: File? = null
    private var recordingStartedAt: Instant? = null

    override val isRecording: Boolean
        get() = synchronized(lock) { recordingKind != null }

    override fun startPreview(): Boolean {
        synchronized(lock) {
            previewRequested = true
            if (camera != null) {
                return true
            }
            val source = cameraFactory.open() ?: run {
                previewRequested = false
                return false
            }
            camera = source
            previewThread = startPreviewThread(source)
            return true
        }
    }

    override fun stopPreview() {
        val closer = synchronized(lock) {
            previewRequested = false
            if (recordingKind == SessionRecordingKind.Video) {
                return
            }
            takeCameraCloser()
        }
        closer?.invoke()
    }

    override fun start(
        sessionId: String,
        kind: SessionRecordingKind,
        microphoneDeviceId: String?,
    ): Boolean {
        val failedCloser = synchronized(lock) {
            if (recordingKind != null) {
                return false
            }
            val startedAt = instantProvider.now()
            val file = fileStore.createFile(sessionId, kind, startedAt)
            when (kind) {
                SessionRecordingKind.Audio -> {
                    val line = microphoneFactory.open(microphoneDeviceId) ?: return false
                    val writer = SessionAudioWavWriter(file)
                    microphone = line
                    wavWriter = writer
                    recordingKind = kind
                    recordingSessionId = sessionId
                    recordingFile = file
                    recordingStartedAt = startedAt
                    audioThread = startAudioThread(line) { bytes, length ->
                        writer.writePcm(bytes, length)
                    }
                    return true
                }
                SessionRecordingKind.Video -> {
                    if (camera == null) {
                        previewRequested = true
                        val source = cameraFactory.open()
                        if (source == null) {
                            previewRequested = false
                            return false
                        }
                        camera = source
                        previewThread = startPreviewThread(source)
                    }
                    val line = microphoneFactory.open(microphoneDeviceId)
                    val encoder = videoEncoderFactory.open(
                        file = file,
                        width = SessionFfmpegCameraFrameSource.WIDTH,
                        height = SessionFfmpegCameraFrameSource.HEIGHT,
                        withAudio = line != null,
                    )
                    if (encoder == null) {
                        line?.close()
                        return@synchronized if (!previewRequested) takeCameraCloser() else null
                    }
                    videoEncoder = encoder
                    microphone = line
                    recordingKind = kind
                    recordingSessionId = sessionId
                    recordingFile = file
                    recordingStartedAt = startedAt
                    if (line != null) {
                        audioThread = startAudioThread(line) { bytes, length ->
                            synchronized(encoderLock) {
                                encoder.recordPcm(bytes, length)
                            }
                        }
                    }
                    return true
                }
            }
        }
        failedCloser?.invoke()
        return false
    }

    override fun stop(): SessionRecording? {
        val snapshot = synchronized(lock) {
            val kind = recordingKind ?: return null
            val sessionId = recordingSessionId ?: return null
            val file = recordingFile ?: return null
            val startedAt = recordingStartedAt ?: return null
            val writer = wavWriter
            val encoder = videoEncoder
            val line = microphone
            val audio = audioThread
            wavWriter = null
            videoEncoder = null
            microphone = null
            audioThread = null
            recordingKind = null
            recordingSessionId = null
            recordingFile = null
            recordingStartedAt = null
            StopWork(
                kind = kind,
                sessionId = sessionId,
                file = file,
                startedAt = startedAt,
                writer = writer,
                encoder = encoder,
                line = line,
                audio = audio,
                closeCamera = !previewRequested,
            )
        }
        snapshot.audio?.interrupt()
        snapshot.line?.close()
        snapshot.audio?.join(1_000)
        snapshot.writer?.close()
        snapshot.encoder?.stop()
        if (snapshot.closeCamera) {
            val closer = synchronized(lock) { takeCameraCloser() }
            closer?.invoke()
        }
        if (!snapshot.file.isFile || snapshot.file.length() == 0L) {
            snapshot.file.delete()
            return null
        }
        return SessionRecording(
            id = snapshot.file.name,
            sessionId = snapshot.sessionId,
            kind = snapshot.kind,
            path = snapshot.file.absolutePath,
            startedAt = snapshot.startedAt,
            byteSize = snapshot.file.length(),
        )
    }

    override fun shutdown() {
        stop()
        val closer = synchronized(lock) {
            previewRequested = false
            takeCameraCloser()
        }
        closer?.invoke()
    }

    private fun startPreviewThread(source: SessionCameraFrameSource): Thread {
        return Thread {
            while (!Thread.currentThread().isInterrupted) {
                val image = source.grabImage() ?: continue
                _previewImages.value = image
                val encoder = synchronized(lock) { videoEncoder }
                if (encoder != null) {
                    synchronized(encoderLock) {
                        encoder.recordImage(image)
                    }
                }
            }
        }.also { thread ->
            thread.isDaemon = true
            thread.name = "session-camera-preview"
            thread.start()
        }
    }

    private fun startAudioThread(
        line: SessionMicrophoneLine,
        onPcm: (ByteArray, Int) -> Unit,
    ): Thread {
        return Thread {
            val buffer = ByteArray(4096)
            while (!Thread.currentThread().isInterrupted) {
                val read = try {
                    line.read(buffer)
                } catch (_: Exception) {
                    break
                }
                if (read > 0) {
                    onPcm(buffer, read)
                }
            }
        }.also { thread ->
            thread.isDaemon = true
            thread.name = "session-mic-record"
            thread.start()
        }
    }

    private fun takeCameraCloser(): (() -> Unit)? {
        val thread = previewThread
        val source = camera
        previewThread = null
        camera = null
        _previewImages.value = null
        if (thread == null && source == null) {
            return null
        }
        return {
            thread?.interrupt()
            source?.stop()
            thread?.join(1_000)
        }
    }

    private data class StopWork(
        val kind: SessionRecordingKind,
        val sessionId: String,
        val file: File,
        val startedAt: Instant,
        val writer: SessionAudioWavWriter?,
        val encoder: SessionVideoEncoder?,
        val line: SessionMicrophoneLine?,
        val audio: Thread?,
        val closeCamera: Boolean,
    )
}

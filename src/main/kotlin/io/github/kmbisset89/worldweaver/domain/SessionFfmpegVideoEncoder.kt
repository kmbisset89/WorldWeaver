package io.github.kmbisset89.worldweaver.domain

import java.awt.image.BufferedImage
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Java2DFrameConverter

internal class SessionFfmpegVideoEncoder private constructor(
    private val recorder: FFmpegFrameRecorder,
) : SessionVideoEncoder {
    private val converter = Java2DFrameConverter()
    private val startNanos = System.nanoTime()

    override fun recordImage(image: BufferedImage) {
        val frame = try {
            converter.convert(image)
        } catch (_: Exception) {
            return
        } ?: return
        try {
            recorder.timestamp = (System.nanoTime() - startNanos) / 1_000L
            recorder.record(frame)
        } catch (_: Exception) {
            return
        }
    }

    override fun recordPcm(pcm: ByteArray, length: Int) {
        if (length < 2) {
            return
        }
        val sampleCount = length / 2
        val samples = ShortArray(sampleCount)
        ByteBuffer.wrap(pcm, 0, length).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples)
        try {
            recorder.recordSamples(ShortBuffer.wrap(samples))
        } catch (_: Exception) {
            return
        }
    }

    override fun stop() {
        runCatching { recorder.stop() }
        runCatching { recorder.release() }
        runCatching { converter.close() }
    }

    companion object : SessionVideoEncoder.Factory {
        override fun open(
            file: File,
            width: Int,
            height: Int,
            withAudio: Boolean,
        ): SessionVideoEncoder? {
            file.parentFile?.mkdirs()
            val h264 = createRecorder(file, width, height, withAudio, preferH264 = true)
            if (h264 != null) {
                return h264
            }
            return createRecorder(file, width, height, withAudio, preferH264 = false)
        }

        private fun createRecorder(
            file: File,
            width: Int,
            height: Int,
            withAudio: Boolean,
            preferH264: Boolean,
        ): SessionFfmpegVideoEncoder? {
            val recorder = FFmpegFrameRecorder(file, width, height)
            recorder.format = "mp4"
            recorder.frameRate = SessionFfmpegCameraFrameSource.FRAME_RATE
            recorder.videoBitrate = 1_000_000
            recorder.videoCodec = if (preferH264) {
                avcodec.AV_CODEC_ID_H264
            } else {
                avcodec.AV_CODEC_ID_MPEG4
            }
            if (withAudio) {
                recorder.audioChannels = SessionAudioWavFormat.CHANNELS
                recorder.sampleRate = SessionAudioWavFormat.SAMPLE_RATE
                recorder.audioBitrate = 128_000
                recorder.audioCodec = avcodec.AV_CODEC_ID_AAC
            } else {
                recorder.audioChannels = 0
            }
            return try {
                recorder.start()
                SessionFfmpegVideoEncoder(recorder)
            } catch (_: Exception) {
                runCatching { recorder.stop() }
                runCatching { recorder.release() }
                null
            }
        }
    }
}

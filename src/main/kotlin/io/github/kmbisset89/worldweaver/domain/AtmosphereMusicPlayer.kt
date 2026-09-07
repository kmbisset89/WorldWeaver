package io.github.kmbisset89.worldweaver.domain

import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FrameGrabber
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.FloatControl
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.UnsupportedAudioFileException

/**
 * Streams one linked music file to the system audio output, with loop and volume.
 */
internal class AtmosphereMusicPlayer {
    sealed interface Result {
        data object Started : Result
        data class Failed(val message: String) : Result
    }

    private val lock = Any()
    private var playback: Playback? = null

    fun play(
        path: String,
        loop: Boolean,
        volume: Int,
        onFinished: () -> Unit,
    ): Result {
        val file = File(path)
        if (!file.isFile) {
            return Result.Failed("That music file is missing")
        }
        stop()
        val next = Playback(
            file = file,
            loopEnabled = loop,
            volume = volume.coerceIn(0, 100),
            onFinished = onFinished,
        )
        if (!next.prepare()) {
            return Result.Failed("Could not play that music file")
        }
        synchronized(lock) {
            playback = next
        }
        next.start()
        return Result.Started
    }

    fun stop() {
        val current = synchronized(lock) {
            val value = playback
            playback = null
            value
        }
        current?.cancel()
    }

    fun setVolume(volume: Int) {
        synchronized(lock) {
            playback?.volume = volume.coerceIn(0, 100)
            playback?.applyVolume()
        }
    }

    fun setLoop(loop: Boolean) {
        synchronized(lock) {
            playback?.loopEnabled = loop
        }
    }

    private class Playback(
        private val file: File,
        @Volatile var loopEnabled: Boolean,
        @Volatile var volume: Int,
        private val onFinished: () -> Unit,
    ) {
        @Volatile
        private var cancelRequested = false
        private var line: SourceDataLine? = null
        private var thread: Thread? = null
        private var firstSource: PcmSource? = null

        fun prepare(): Boolean {
            val source = openSource()
            firstSource = source
            return source != null
        }

        fun start() {
            val thread = Thread(
                { runPlayback() },
                "atmosphere-music",
            )
            thread.isDaemon = true
            this.thread = thread
            thread.start()
        }

        fun cancel() {
            cancelRequested = true
            val pending = firstSource
            firstSource = null
            pending?.close()
            val currentLine = line
            if (currentLine != null) {
                runCatching { currentLine.stop() }
                runCatching { currentLine.flush() }
                runCatching { currentLine.close() }
            }
            thread?.interrupt()
        }

        fun applyVolume() {
            val currentLine = line ?: return
            applyVolumeTo(currentLine, volume)
        }

        private fun runPlayback() {
            var finishedNaturally = false
            try {
                var source = firstSource
                firstSource = null
                do {
                    if (cancelRequested) {
                        source?.close()
                        break
                    }
                    val decoded = source ?: openSource() ?: throw IllegalStateException("unsupported")
                    source = null
                    try {
                        playOnce(decoded)
                    } finally {
                        decoded.close()
                    }
                } while (!cancelRequested && loopEnabled)
                finishedNaturally = !cancelRequested
            } catch (_: Exception) {
                finishedNaturally = false
            } finally {
                val currentLine = line
                line = null
                if (currentLine != null) {
                    runCatching { currentLine.drain() }
                    runCatching { currentLine.stop() }
                    runCatching { currentLine.close() }
                }
            }
            if (finishedNaturally) {
                onFinished()
            }
        }

        private fun playOnce(source: PcmSource) {
            val format = source.format
            val currentLine = line
            val output = if (currentLine != null && currentLine.format.matches(format)) {
                currentLine
            } else {
                currentLine?.let {
                    runCatching { it.stop() }
                    runCatching { it.close() }
                }
                val info = DataLine.Info(SourceDataLine::class.java, format)
                if (!AudioSystem.isLineSupported(info)) {
                    throw IllegalStateException("unsupported line")
                }
                val opened = AudioSystem.getSourceDataLine(format)
                opened.open(format)
                applyVolumeTo(opened, volume)
                opened.start()
                line = opened
                opened
            }
            val buffer = ByteArray(BUFFER_SIZE)
            while (!cancelRequested) {
                val read = source.read(buffer)
                if (read < 0) {
                    return
                }
                if (read == 0) {
                    continue
                }
                var offset = 0
                while (offset < read && !cancelRequested) {
                    val written = output.write(buffer, offset, read - offset)
                    if (written <= 0) {
                        return
                    }
                    offset += written
                }
            }
        }

        private fun openSource(): PcmSource? {
            javaSoundSource()?.let { return it }
            return ffmpegSource()
        }

        private fun javaSoundSource(): PcmSource? {
            return try {
                val original = AudioSystem.getAudioInputStream(file)
                val sourceFormat = original.format
                val pcm = if (isPcm16(sourceFormat)) {
                    original
                } else {
                    val target = AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        sourceFormat.sampleRate,
                        16,
                        sourceFormat.channels,
                        sourceFormat.channels * 2,
                        sourceFormat.sampleRate,
                        false,
                    )
                    AudioSystem.getAudioInputStream(target, original)
                }
                StreamPcmSource(pcm)
            } catch (_: UnsupportedAudioFileException) {
                null
            } catch (_: Exception) {
                null
            }
        }

        private fun ffmpegSource(): PcmSource? {
            return try {
                val grabber = FFmpegFrameGrabber(file)
                grabber.sampleMode = FrameGrabber.SampleMode.SHORT
                grabber.start()
                if (grabber.audioChannels <= 0 || grabber.sampleRate <= 0) {
                    runCatching { grabber.stop() }
                    runCatching { grabber.release() }
                    return null
                }
                FfmpegPcmSource(grabber)
            } catch (_: Exception) {
                null
            }
        }

        private fun isPcm16(format: AudioFormat): Boolean {
            return format.encoding == AudioFormat.Encoding.PCM_SIGNED &&
                format.sampleSizeInBits == 16 &&
                !format.isBigEndian
        }

        private fun applyVolumeTo(output: SourceDataLine, volume: Int) {
            if (!output.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                return
            }
            val control = output.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
            val gain = if (volume <= 0) {
                control.minimum
            } else {
                val linear = volume / 100.0
                val db = (20.0 * kotlin.math.ln(linear) / kotlin.math.ln(10.0)).toFloat()
                db.coerceIn(control.minimum, control.maximum)
            }
            control.value = gain
        }
    }

    private interface PcmSource {
        val format: AudioFormat
        fun read(buffer: ByteArray): Int
        fun close()
    }

    private class StreamPcmSource(
        private val stream: javax.sound.sampled.AudioInputStream,
    ) : PcmSource {
        override val format: AudioFormat = stream.format

        override fun read(buffer: ByteArray): Int {
            return stream.read(buffer)
        }

        override fun close() {
            runCatching { stream.close() }
        }
    }

    private class FfmpegPcmSource(
        private val grabber: FFmpegFrameGrabber,
    ) : PcmSource {
        override val format: AudioFormat = AudioFormat(
            grabber.sampleRate.toFloat(),
            16,
            grabber.audioChannels,
            true,
            false,
        )
        private var pending: ByteArray = ByteArray(0)
        private var pendingOffset = 0

        override fun read(buffer: ByteArray): Int {
            if (pendingOffset < pending.size) {
                val copy = minOf(buffer.size, pending.size - pendingOffset)
                System.arraycopy(pending, pendingOffset, buffer, 0, copy)
                pendingOffset += copy
                return copy
            }
            val frame = grabber.grabSamples() ?: return -1
            val samples = frame.samples ?: return 0
            if (samples.isEmpty()) {
                return 0
            }
            val shortBuffer = samples[0] as? ShortBuffer ?: return 0
            val remaining = shortBuffer.remaining()
            if (remaining <= 0) {
                return 0
            }
            pending = ByteArray(remaining * 2)
            ByteBuffer.wrap(pending).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shortBuffer)
            pendingOffset = 0
            val copy = minOf(buffer.size, pending.size)
            System.arraycopy(pending, 0, buffer, 0, copy)
            pendingOffset = copy
            return copy
        }

        override fun close() {
            runCatching { grabber.stop() }
            runCatching { grabber.release() }
        }
    }

    private companion object {
        const val BUFFER_SIZE = 4096
    }
}

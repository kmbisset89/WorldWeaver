package io.github.kmbisset89.worldweaver.domain

import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.Mixer
import javax.sound.sampled.TargetDataLine

internal class SessionJavaSoundMicrophoneLine private constructor(
    private val line: TargetDataLine,
) : SessionMicrophoneLine {
    override fun read(buffer: ByteArray): Int {
        return line.read(buffer, 0, buffer.size)
    }

    override fun close() {
        runCatching { line.stop() }
        runCatching { line.close() }
    }

    companion object : SessionMicrophoneLine.Factory {
        fun listDevices(): List<SessionMicrophoneDevice> {
            val format = SessionAudioWavFormat.recordFormat
            val info = DataLine.Info(TargetDataLine::class.java, format)
            return AudioSystem.getMixerInfo().mapNotNull { mixerInfo ->
                val mixer = try {
                    AudioSystem.getMixer(mixerInfo)
                } catch (_: Exception) {
                    return@mapNotNull null
                }
                if (!mixerSupportsTarget(mixer, info)) {
                    return@mapNotNull null
                }
                SessionMicrophoneDevice(
                    id = mixerInfo.name,
                    label = mixerInfo.name,
                )
            }.distinctBy { device -> device.id }
        }

        override fun open(deviceId: String?): SessionMicrophoneLine? {
            val format = SessionAudioWavFormat.recordFormat
            val info = DataLine.Info(TargetDataLine::class.java, format)
            val target = try {
                lineFor(deviceId, info)
            } catch (_: Exception) {
                return null
            } ?: return null
            return try {
                target.open(format)
                target.start()
                SessionJavaSoundMicrophoneLine(target)
            } catch (_: Exception) {
                runCatching { target.close() }
                null
            }
        }

        private fun lineFor(deviceId: String?, info: DataLine.Info): TargetDataLine? {
            if (deviceId.isNullOrBlank()) {
                if (!AudioSystem.isLineSupported(info)) {
                    return null
                }
                return AudioSystem.getLine(info) as TargetDataLine
            }
            val mixerInfo = AudioSystem.getMixerInfo().firstOrNull { candidate ->
                candidate.name == deviceId
            } ?: return lineFor(null, info)
            val mixer = AudioSystem.getMixer(mixerInfo)
            return mixer.getLine(info) as TargetDataLine
        }

        private fun mixerSupportsTarget(mixer: Mixer, info: DataLine.Info): Boolean {
            if (mixer.isLineSupported(info)) {
                return true
            }
            return mixer.targetLineInfo.any { lineInfo ->
                lineInfo is DataLine.Info &&
                    TargetDataLine::class.java.isAssignableFrom(lineInfo.lineClass)
            }
        }
    }
}

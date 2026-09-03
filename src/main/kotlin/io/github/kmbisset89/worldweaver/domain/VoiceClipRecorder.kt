package io.github.kmbisset89.worldweaver.domain

import java.io.ByteArrayOutputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine

internal class VoiceClipRecorder {
    private val lock = Any()
    private var line: TargetDataLine? = null
    private var buffer: ByteArrayOutputStream? = null
    private var worker: Thread? = null

    fun start(): Boolean {
        synchronized(lock) {
            if (line != null) {
                return false
            }
            val format = VoiceClipWavFormat.recordFormat
            val info = DataLine.Info(TargetDataLine::class.java, format)
            if (!AudioSystem.isLineSupported(info)) {
                return false
            }
            val target = try {
                AudioSystem.getLine(info) as TargetDataLine
            } catch (_: Exception) {
                return false
            }
            return try {
                target.open(format)
                val out = ByteArrayOutputStream()
                target.start()
                line = target
                buffer = out
                worker = Thread {
                    val chunk = ByteArray(4096)
                    while (!Thread.currentThread().isInterrupted) {
                        val read = try {
                            target.read(chunk, 0, chunk.size)
                        } catch (_: Exception) {
                            break
                        }
                        if (read > 0) {
                            synchronized(out) {
                                out.write(chunk, 0, read)
                            }
                        }
                    }
                }.also { thread ->
                    thread.isDaemon = true
                    thread.start()
                }
                true
            } catch (_: Exception) {
                runCatching { target.close() }
                line = null
                buffer = null
                worker = null
                false
            }
        }
    }

    fun stop(): ByteArray? {
        val pcm = synchronized(lock) {
            val target = line ?: return null
            val out = buffer
            worker?.interrupt()
            runCatching { target.stop() }
            runCatching { target.close() }
            worker?.join(500)
            line = null
            buffer = null
            worker = null
            if (out == null) {
                return null
            }
            synchronized(out) {
                out.toByteArray()
            }
        }
        if (pcm.isEmpty()) {
            return null
        }
        return VoiceClipWavFormat.wrapPcm(pcm)
    }
}

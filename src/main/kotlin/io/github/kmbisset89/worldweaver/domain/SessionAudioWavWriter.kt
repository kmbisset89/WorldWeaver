package io.github.kmbisset89.worldweaver.domain

import java.io.File
import java.io.RandomAccessFile

/**
 * Streams 16-bit PCM into a WAV file, rewriting the header length when closed.
 */
internal class SessionAudioWavWriter(
    file: File,
) : AutoCloseable {
    private val stream = RandomAccessFile(file, "rw")
    private var pcmBytes = 0

    init {
        file.parentFile?.mkdirs()
        stream.setLength(0)
        stream.write(SessionAudioWavFormat.header(0))
    }

    fun writePcm(bytes: ByteArray, length: Int) {
        if (length <= 0) {
            return
        }
        stream.write(bytes, 0, length)
        pcmBytes += length
    }

    override fun close() {
        stream.seek(0)
        stream.write(SessionAudioWavFormat.header(pcmBytes))
        stream.close()
    }
}

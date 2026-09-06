package io.github.kmbisset89.worldweaver.domain

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import javax.sound.sampled.AudioFormat

internal object SessionAudioWavFormat {
    const val SAMPLE_RATE = 44_100
    const val CHANNELS = 1
    const val BITS_PER_SAMPLE = 16
    const val HEADER_SIZE = 44

    val recordFormat: AudioFormat = AudioFormat(
        SAMPLE_RATE.toFloat(),
        BITS_PER_SAMPLE,
        CHANNELS,
        true,
        false,
    )

    fun isValid(bytes: ByteArray): Boolean {
        if (bytes.size < HEADER_SIZE) {
            return false
        }
        if (ascii(bytes, 0, 4) != "RIFF" || ascii(bytes, 8, 4) != "WAVE") {
            return false
        }
        var offset = 12
        while (offset + 8 <= bytes.size) {
            val chunkId = ascii(bytes, offset, 4)
            val chunkSize = readInt32Le(bytes, offset + 4)
            if (chunkSize < 0) {
                return false
            }
            val dataStart = offset + 8
            if (chunkId == "data") {
                return chunkSize > 0 && dataStart + chunkSize <= bytes.size
            }
            offset = dataStart + chunkSize
            if (chunkSize % 2 == 1) {
                offset += 1
            }
        }
        return false
    }

    fun header(pcmSize: Int): ByteArray {
        val blockAlign = CHANNELS * (BITS_PER_SAMPLE / 8)
        val byteRate = SAMPLE_RATE * blockAlign
        val buffer = ByteBuffer.allocate(HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(asciiBytes("RIFF"))
        buffer.putInt(36 + pcmSize)
        buffer.put(asciiBytes("WAVE"))
        buffer.put(asciiBytes("fmt "))
        buffer.putInt(16)
        buffer.putShort(1)
        buffer.putShort(CHANNELS.toShort())
        buffer.putInt(SAMPLE_RATE)
        buffer.putInt(byteRate)
        buffer.putShort(blockAlign.toShort())
        buffer.putShort(BITS_PER_SAMPLE.toShort())
        buffer.put(asciiBytes("data"))
        buffer.putInt(pcmSize)
        return buffer.array()
    }

    private fun ascii(bytes: ByteArray, offset: Int, length: Int): String {
        return String(bytes, offset, length, StandardCharsets.US_ASCII)
    }

    private fun asciiBytes(value: String): ByteArray {
        return value.toByteArray(StandardCharsets.US_ASCII)
    }

    private fun readInt32Le(bytes: ByteArray, offset: Int): Int {
        return ByteBuffer.wrap(bytes, offset, 4).order(ByteOrder.LITTLE_ENDIAN).int
    }
}

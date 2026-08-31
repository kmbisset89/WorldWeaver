package net.tactware.worldweaver.domain

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import javax.sound.sampled.AudioFormat

internal object VoiceClipWavFormat {
    const val SAMPLE_RATE = 22_050
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

    fun wrapPcm(pcm: ByteArray): ByteArray {
        val blockAlign = CHANNELS * (BITS_PER_SAMPLE / 8)
        val byteRate = SAMPLE_RATE * blockAlign
        val header = ByteBuffer.allocate(HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN)
        header.put(asciiBytes("RIFF"))
        header.putInt(36 + pcm.size)
        header.put(asciiBytes("WAVE"))
        header.put(asciiBytes("fmt "))
        header.putInt(16)
        header.putShort(1)
        header.putShort(CHANNELS.toShort())
        header.putInt(SAMPLE_RATE)
        header.putInt(byteRate)
        header.putShort(blockAlign.toShort())
        header.putShort(BITS_PER_SAMPLE.toShort())
        header.put(asciiBytes("data"))
        header.putInt(pcm.size)
        return header.array() + pcm
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

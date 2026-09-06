package io.github.kmbisset89.worldweaver.domain

internal interface SessionMicrophoneLine {
    fun read(buffer: ByteArray): Int
    fun close()

    fun interface Factory {
        fun open(deviceId: String?): SessionMicrophoneLine?
    }
}

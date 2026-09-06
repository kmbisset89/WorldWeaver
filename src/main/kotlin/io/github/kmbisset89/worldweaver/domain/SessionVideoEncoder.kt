package io.github.kmbisset89.worldweaver.domain

import java.awt.image.BufferedImage
import java.io.File

internal interface SessionVideoEncoder {
    fun recordImage(image: BufferedImage)
    fun recordPcm(pcm: ByteArray, length: Int)
    fun stop()

    fun interface Factory {
        fun open(file: File, width: Int, height: Int, withAudio: Boolean): SessionVideoEncoder?
    }
}

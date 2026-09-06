package io.github.kmbisset89.worldweaver.domain

import java.awt.image.BufferedImage

internal interface SessionCameraFrameSource {
    fun grabImage(): BufferedImage?
    fun stop()

    fun interface Factory {
        fun open(): SessionCameraFrameSource?
    }
}

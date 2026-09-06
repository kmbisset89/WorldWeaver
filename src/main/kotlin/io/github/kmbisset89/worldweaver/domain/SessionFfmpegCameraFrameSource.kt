package io.github.kmbisset89.worldweaver.domain

import java.awt.image.BufferedImage
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Java2DFrameConverter

internal class SessionFfmpegCameraFrameSource private constructor(
    private val grabber: FFmpegFrameGrabber,
) : SessionCameraFrameSource {
    private val converter = Java2DFrameConverter()

    override fun grabImage(): BufferedImage? {
        val frame = try {
            grabber.grabImage()
        } catch (_: Exception) {
            return null
        } ?: return null
        return try {
            converter.convert(frame)
        } catch (_: Exception) {
            null
        }
    }

    override fun stop() {
        runCatching { grabber.stop() }
        runCatching { grabber.release() }
        runCatching { converter.close() }
    }

    companion object : SessionCameraFrameSource.Factory {
        const val WIDTH = 640
        const val HEIGHT = 480
        const val FRAME_RATE = 15.0

        override fun open(): SessionCameraFrameSource? {
            candidateGrabbers().forEach { grabber ->
                val started = try {
                    grabber.imageWidth = WIDTH
                    grabber.imageHeight = HEIGHT
                    grabber.frameRate = FRAME_RATE
                    grabber.start()
                    true
                } catch (_: Exception) {
                    runCatching { grabber.stop() }
                    runCatching { grabber.release() }
                    false
                }
                if (started) {
                    return SessionFfmpegCameraFrameSource(grabber)
                }
            }
            return null
        }

        private fun candidateGrabbers(): List<FFmpegFrameGrabber> {
            val osName = System.getProperty("os.name").lowercase()
            return when {
                osName.contains("mac") -> listOf(
                    avFoundation("0"),
                    avFoundation("0:none"),
                )
                osName.contains("windows") -> listOf(
                    dshow("video=Integrated Camera"),
                    dshow("video=USB Camera"),
                    dshow("video=HD Webcam"),
                )
                else -> listOf(
                    v4l2("/dev/video0"),
                    v4l2("/dev/video1"),
                )
            }
        }

        private fun avFoundation(filename: String): FFmpegFrameGrabber {
            return FFmpegFrameGrabber(filename).apply {
                format = "avfoundation"
                setOption("framerate", FRAME_RATE.toInt().toString())
            }
        }

        private fun dshow(filename: String): FFmpegFrameGrabber {
            return FFmpegFrameGrabber(filename).apply {
                format = "dshow"
                setOption("framerate", FRAME_RATE.toInt().toString())
            }
        }

        private fun v4l2(filename: String): FFmpegFrameGrabber {
            return FFmpegFrameGrabber(filename).apply {
                format = "v4l2"
                frameRate = FRAME_RATE
            }
        }
    }
}

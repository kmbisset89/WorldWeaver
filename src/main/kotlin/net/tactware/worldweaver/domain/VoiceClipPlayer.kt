package net.tactware.worldweaver.domain

import java.io.File
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.LineEvent

internal class VoiceClipPlayer {
    private val lock = Any()
    private var clip: Clip? = null

    fun play(path: String, onFinished: () -> Unit): Boolean {
        stop()
        val file = File(path)
        if (!file.isFile) {
            return false
        }
        return try {
            val stream = AudioSystem.getAudioInputStream(file)
            val next = AudioSystem.getClip()
            next.open(stream)
            next.addLineListener { event ->
                if (event.type == LineEvent.Type.STOP) {
                    onFinished()
                }
            }
            synchronized(lock) {
                clip = next
            }
            next.start()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun stop() {
        val current = synchronized(lock) {
            val value = clip
            clip = null
            value
        }
        if (current != null) {
            runCatching { current.stop() }
            runCatching { current.close() }
        }
    }
}

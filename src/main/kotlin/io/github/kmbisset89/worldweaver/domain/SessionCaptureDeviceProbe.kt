package io.github.kmbisset89.worldweaver.domain

import java.io.File

internal class SessionCaptureDeviceProbe(
    private val listMicrophones: () -> List<SessionMicrophoneDevice> =
        SessionJavaSoundMicrophoneLine::listDevices,
    private val cameraPresent: () -> Boolean = Companion::defaultCameraPresent,
) {
    fun microphones(): List<SessionMicrophoneDevice> = listMicrophones()

    fun hasMicrophone(): Boolean = microphones().isNotEmpty()

    fun hasCamera(): Boolean = cameraPresent()

    private companion object {
        fun defaultCameraPresent(): Boolean {
            val osName = System.getProperty("os.name").lowercase()
            if (osName.contains("linux")) {
                return File("/dev/video0").exists() || File("/dev/video1").exists()
            }
            return true
        }
    }
}

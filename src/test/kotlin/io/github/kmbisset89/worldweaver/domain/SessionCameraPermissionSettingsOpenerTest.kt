package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SessionCameraPermissionSettingsOpenerTest {
    @Test
    fun macOpensCameraPrivacyPane() {
        var command: List<String>? = null
        val opener = SessionCameraPermissionSettingsOpener(
            osName = { "Mac OS X" },
            startProcess = { args ->
                command = args
                true
            },
        )

        assertTrue(opener.open())
        assertEquals(
            listOf("open", "x-apple.systempreferences:com.apple.preference.security?Privacy_Camera"),
            command,
        )
    }

    @Test
    fun windowsOpensWebcamPrivacySettings() {
        var command: List<String>? = null
        val opener = SessionCameraPermissionSettingsOpener(
            osName = { "Windows 11" },
            startProcess = { args ->
                command = args
                true
            },
        )

        assertTrue(opener.open())
        assertEquals(listOf("cmd", "/c", "start", "ms-settings:privacy-webcam"), command)
    }

    @Test
    fun linuxDoesNotOpenSettings() {
        val opener = SessionCameraPermissionSettingsOpener(
            osName = { "Linux" },
            startProcess = { error("should not start a process") },
        )

        assertFalse(opener.open())
    }
}

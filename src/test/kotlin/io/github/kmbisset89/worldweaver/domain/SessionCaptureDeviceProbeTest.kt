package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SessionCaptureDeviceProbeTest {
    @Test
    fun hasMicrophoneFollowsListedDevices() {
        val probe = SessionCaptureDeviceProbe(
            listMicrophones = {
                listOf(SessionMicrophoneDevice("built-in", "Built-in Microphone"))
            },
            cameraPresent = { false },
        )

        assertTrue(probe.hasMicrophone())
        assertEquals("built-in", probe.microphones().single().id)
        assertEquals(false, probe.hasCamera())
    }
}

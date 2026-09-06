package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class ScanGoveeDevicesUseCaseTest {
    private val preferences = Preferences.userRoot().node(TEST_NODE)
    private val client = FakeGoveeLightingClient()

    @AfterTest
    fun tearDown() {
        preferences.removeNode()
    }

    @Test
    fun persistsScannedDevices() = runTest {
        val devices = listOf(GoveeDevice("AA:BB", "192.168.1.50", "H6072"))
        client.scanResult = GoveeLightingClient.ScanResult.Devices(devices)
        val store = AtmosphereSettingsStore(preferences)
        val found = assertIs<ScanGoveeDevicesUseCase.Result.Found>(
            ScanGoveeDevicesUseCase(client, store)(),
        )
        assertEquals(devices, found.devices)
        assertEquals(devices, store.settings.value.goveeDevices)
    }

    @Test
    fun reportsScanFailure() = runTest {
        client.scanResult = GoveeLightingClient.ScanResult.Failed("No Govee lights answered")
        val result = ScanGoveeDevicesUseCase(client, AtmosphereSettingsStore(preferences))()
        val failed = assertIs<ScanGoveeDevicesUseCase.Result.Failed>(result)
        assertEquals("No Govee lights answered", failed.message)
    }

    private companion object {
        const val TEST_NODE = "io.github.kmbisset89.worldweaver.test.govee.scan"
    }
}

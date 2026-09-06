package io.github.kmbisset89.worldweaver.domain

import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class AtmosphereSettingsStoreTest {
    private val preferences = Preferences.userRoot().node(TEST_NODE)

    @AfterTest
    fun tearDown() {
        preferences.removeNode()
    }

    @Test
    fun roundTripsConnectionScenesAndAlwaysOnTop() {
        val store = AtmosphereSettingsStore(preferences)
        val scenes = listOf(
            AtmosphereScene(id = "s1", name = "Tavern", entityId = "scene.tavern", sortOrder = 0),
        )
        store.setConnection(HomeAssistantConnection("http://ha.local:8123", "token"))
        store.setHueConnection(HueConnection("192.168.1.40", "hue-key"))
        store.setGoveeDevices(listOf(GoveeDevice("AA:BB", "192.168.1.50", "H6072")))
        store.setHueLights(listOf(HueLight("light-1", "Table lamp")))
        store.setScenes(scenes)
        store.setMoods(
            listOf(AtmosphereMood("m1", "Temple", true, 40, "#C4A35A", 0)),
        )
        store.setSelectedHueLightIds(listOf("light-1"))
        store.setSelectedGoveeDeviceIds(listOf("AA:BB"))
        store.setAlwaysOnTop(true)

        val reloaded = AtmosphereSettingsStore(preferences).settings.value
        assertEquals("http://ha.local:8123", reloaded.connection.baseUrl)
        assertEquals("token", reloaded.connection.token)
        assertEquals("192.168.1.40", reloaded.hue.bridgeHost)
        assertEquals("hue-key", reloaded.hue.applicationKey)
        assertEquals(listOf(GoveeDevice("AA:BB", "192.168.1.50", "H6072")), reloaded.goveeDevices)
        assertEquals(listOf(HueLight("light-1", "Table lamp")), reloaded.hueLights)
        assertEquals(scenes, reloaded.scenes)
        assertEquals(
            listOf(AtmosphereMood("m1", "Temple", true, 40, "#C4A35A", 0)),
            reloaded.moods,
        )
        assertEquals(listOf("light-1"), reloaded.selectedHueLightIds)
        assertEquals(listOf("AA:BB"), reloaded.selectedGoveeDeviceIds)
        assertEquals(true, reloaded.isAlwaysOnTop)
    }

    @Test
    fun defaultsToEmptyWhenUnset() {
        val settings = AtmosphereSettingsStore(preferences).settings.value
        assertFalse(settings.connection.isConfigured)
        assertFalse(settings.hue.isConfigured)
        assertEquals(emptyList(), settings.goveeDevices)
        assertEquals(emptyList(), settings.hueLights)
        assertEquals(emptyList(), settings.moods)
        assertEquals(emptyList(), settings.selectedHueLightIds)
        assertEquals(emptyList(), settings.selectedGoveeDeviceIds)
        assertFalse(settings.isAlwaysOnTop)
    }

    private companion object {
        const val TEST_NODE = "io.github.kmbisset89.worldweaver.test.atmosphere.store"
    }
}

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
        store.setLookTransitionMs(1_200)
        store.setMusicTracks(
            listOf(
                AtmosphereMusicTrack("t1", "Tavern", "/tmp/tavern.mp3", 0),
            ),
        )
        store.setMusicVolume(55)
        store.setMusicLoopEnabled(false)

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
        assertEquals(1_200, reloaded.lookTransitionMs)
        assertEquals(
            listOf(AtmosphereMusicTrack("t1", "Tavern", "/tmp/tavern.mp3", 0)),
            reloaded.musicTracks,
        )
        assertEquals(55, reloaded.musicVolume)
        assertEquals(false, reloaded.musicLoopEnabled)
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
        assertEquals(LightingTransitionCalculator.DEFAULT_DURATION_MS, settings.lookTransitionMs)
        assertEquals(emptyList(), settings.musicTracks)
        assertEquals(AtmosphereSettings.DEFAULT_MUSIC_VOLUME, settings.musicVolume)
        assertEquals(AtmosphereSettings.DEFAULT_MUSIC_LOOP, settings.musicLoopEnabled)
    }

    private companion object {
        const val TEST_NODE = "io.github.kmbisset89.worldweaver.test.atmosphere.store"
    }
}

package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class ListHueLightsUseCaseTest {
    private val preferences = Preferences.userRoot().node(TEST_NODE)
    private val client = FakeHueClient()

    @AfterTest
    fun tearDown() {
        preferences.removeNode()
    }

    @Test
    fun persistsListedLights() = runTest {
        val lights = listOf(HueLight("light-1", "Table lamp"))
        client.lightListResult = HueClient.LightListResult.Lights(lights)
        val store = AtmosphereSettingsStore(preferences)
        store.setHueConnection(HueConnection("192.168.1.40", "hue-key"))
        val listed = assertIs<ListHueLightsUseCase.Result.Listed>(ListHueLightsUseCase(store, client)())
        assertEquals(lights, listed.lights)
        assertEquals(lights, store.settings.value.hueLights)
    }

    @Test
    fun notConfiguredWhenHueMissing() = runTest {
        val store = AtmosphereSettingsStore(preferences)
        assertIs<ListHueLightsUseCase.Result.NotConfigured>(ListHueLightsUseCase(store, client)())
    }

    private companion object {
        const val TEST_NODE = "io.github.kmbisset89.worldweaver.test.hue.lights"
    }
}

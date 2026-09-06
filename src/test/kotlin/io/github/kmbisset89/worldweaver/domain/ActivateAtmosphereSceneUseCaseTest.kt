package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

internal class ActivateAtmosphereSceneUseCaseTest {
    private val preferences = Preferences.userRoot().node(TEST_NODE)
    private val homeAssistant = FakeHomeAssistantClient()
    private val hue = FakeHueClient()
    private val govee = FakeGoveeLightingClient()

    @AfterTest
    fun tearDown() {
        preferences.removeNode()
    }

    @Test
    fun activatesHomeAssistantScene() = runTest {
        val result = useCase(configuredStore())("s1")

        assertIs<ActivateAtmosphereSceneUseCase.Result.Activated>(result)
        assertEquals("scene.tavern", homeAssistant.lastActivatedEntityId)
    }

    @Test
    fun activatesHueAndGoveeTogether() = runTest {
        val store = AtmosphereSettingsStore(preferences)
        store.setHueConnection(HueConnection("192.168.1.40", "hue-key"))
        store.setGoveeDevices(listOf(GoveeDevice("AA:BB", "192.168.1.50", "H6072")))
        store.setScenes(
            listOf(
                AtmosphereScene(
                    id = "s1",
                    name = "Combat",
                    sortOrder = 0,
                    hueSceneId = "hue-combat",
                    hueGroupId = "1",
                    hueLightIds = listOf("light-1"),
                    goveeDeviceIds = listOf("AA:BB"),
                    goveePowerOn = true,
                    goveeBrightness = 90,
                    goveeColorHex = "#C41E3A",
                ),
            ),
        )

        val result = useCase(store)("s1")

        assertIs<ActivateAtmosphereSceneUseCase.Result.Activated>(result)
        assertEquals("hue-combat", hue.lastActivatedSceneId)
        assertEquals(listOf("light-1"), hue.lastActivatedLightIds)
        assertEquals(listOf("AA:BB"), govee.lastDevices.map { it.deviceId })
        assertEquals(196, govee.lastCommand?.red)
    }

    @Test
    fun appliesHueLookWithoutScene() = runTest {
        val store = AtmosphereSettingsStore(preferences)
        store.setHueConnection(HueConnection("192.168.1.40", "hue-key"))
        store.setScenes(
            listOf(
                AtmosphereScene(
                    id = "s1",
                    name = "Warm",
                    sortOrder = 0,
                    hueLightIds = listOf("light-1", "light-2"),
                    goveePowerOn = true,
                    goveeBrightness = 55,
                    goveeColorHex = "#E39B5A",
                ),
            ),
        )

        val result = useCase(store)("s1")

        assertIs<ActivateAtmosphereSceneUseCase.Result.Activated>(result)
        assertNull(hue.lastActivatedSceneId)
        assertEquals(listOf("light-1", "light-2"), hue.lastColorLightIds)
        assertEquals(true, hue.lastColorPowerOn)
        assertEquals(55, hue.lastColorBrightness)
        assertEquals(227, hue.lastColorRed)
        assertEquals(155, hue.lastColorGreen)
        assertEquals(90, hue.lastColorBlue)
    }

    @Test
    fun failedWhenHomeAssistantMissing() = runTest {
        val store = AtmosphereSettingsStore(preferences)
        store.setScenes(listOf(AtmosphereScene("s1", "Tavern", "scene.tavern", 0)))
        val failed = assertIs<ActivateAtmosphereSceneUseCase.Result.Failed>(useCase(store)("s1"))
        assertEquals("Home Assistant is not connected.", failed.message)
        assertNull(homeAssistant.lastActivatedEntityId)
    }

    @Test
    fun notFoundWhenSceneMissing() = runTest {
        assertIs<ActivateAtmosphereSceneUseCase.Result.NotFound>(useCase(configuredStore())("missing"))
    }

    @Test
    fun failedWhenTokenRejected() = runTest {
        homeAssistant.activateResult = HomeAssistantClient.ActivateResult.Unauthorized
        val failed = assertIs<ActivateAtmosphereSceneUseCase.Result.Failed>(useCase(configuredStore())("s1"))
        assertEquals("Home Assistant rejected the token.", failed.message)
    }

    @Test
    fun failedWhenClientFails() = runTest {
        homeAssistant.activateResult = HomeAssistantClient.ActivateResult.Failed("Could not reach Home Assistant")
        val failed = assertIs<ActivateAtmosphereSceneUseCase.Result.Failed>(useCase(configuredStore())("s1"))
        assertEquals("Could not reach Home Assistant", failed.message)
    }

    private fun useCase(store: AtmosphereSettingsStore): ActivateAtmosphereSceneUseCase {
        return ActivateAtmosphereSceneUseCase(
            store,
            homeAssistant,
            hue,
            ApplyAtmosphereLookUseCase(store, hue, govee),
        )
    }

    private fun configuredStore(): AtmosphereSettingsStore {
        val store = AtmosphereSettingsStore(preferences)
        store.setConnection(HomeAssistantConnection("http://ha.local:8123", "token"))
        store.setScenes(listOf(AtmosphereScene("s1", "Tavern", "scene.tavern", 0)))
        return store
    }

    private companion object {
        const val TEST_NODE = "io.github.kmbisset89.worldweaver.test.atmosphere.activate"
    }
}

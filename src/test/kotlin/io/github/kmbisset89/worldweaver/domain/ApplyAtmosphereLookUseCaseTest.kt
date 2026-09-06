package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

internal class ApplyAtmosphereLookUseCaseTest {
    private val preferences = Preferences.userRoot().node(TEST_NODE)
    private val hue = FakeHueClient()
    private val govee = FakeGoveeLightingClient()

    @AfterTest
    fun tearDown() {
        preferences.removeNode()
    }

    @Test
    fun appliesHueColorToSelectedLights() = runTest {
        val store = AtmosphereSettingsStore(preferences)
        store.setHueConnection(HueConnection("192.168.1.40", "hue-key"))

        val result = useCase(store)(
            hueLightIds = listOf("light-1"),
            powerOn = true,
            brightness = 90,
            colorHex = "#C41E3A",
        )

        assertIs<ApplyAtmosphereLookUseCase.Result.Applied>(result)
        assertEquals(listOf("light-1"), hue.lastColorLightIds)
        assertEquals(196, hue.lastColorRed)
        assertEquals(90, hue.lastColorBrightness)
        assertNull(hue.lastActivatedSceneId)
    }

    @Test
    fun failedWhenHueMissing() = runTest {
        val failed = assertIs<ApplyAtmosphereLookUseCase.Result.Failed>(
            useCase(AtmosphereSettingsStore(preferences))(
                hueLightIds = listOf("light-1"),
                powerOn = true,
                brightness = 55,
                colorHex = "#E39B5A",
            ),
        )
        assertEquals("Philips Hue is not connected.", failed.message)
    }

    @Test
    fun noTargetsWhenNothingSelected() = runTest {
        assertIs<ApplyAtmosphereLookUseCase.Result.NoTargets>(
            useCase(AtmosphereSettingsStore(preferences))(
                powerOn = true,
                brightness = 55,
                colorHex = "#E39B5A",
            ),
        )
    }

    private fun useCase(store: AtmosphereSettingsStore): ApplyAtmosphereLookUseCase {
        return ApplyAtmosphereLookUseCase(store, hue, govee)
    }

    private companion object {
        const val TEST_NODE = "io.github.kmbisset89.worldweaver.test.atmosphere.applylook"
    }
}

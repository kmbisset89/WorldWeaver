package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class PlayAtmosphereLightingEffectUseCaseTest {
    private val preferences = Preferences.userRoot().node(TEST_NODE)
    private val hue = FakeHueClient()
    private val govee = FakeGoveeLightingClient()

    @AfterTest
    fun tearDown() {
        preferences.removeNode()
    }

    @Test
    fun lightningAppliesWhiteThenRestoresTheCurrentLook() = runTest {
        val store = AtmosphereSettingsStore(preferences)
        store.setHueConnection(HueConnection("192.168.1.40", "hue-key"))
        val current = LightingLook(powerOn = true, brightness = 55, red = 227, green = 155, blue = 90)

        val result = useCase(store)(
            effect = AtmosphereLightingEffect.Lightning,
            currentLook = current,
            hueLightIds = listOf("light-1"),
        )

        assertIs<PlayAtmosphereLightingEffectUseCase.Result.Played>(result)
        assertTrue(hue.appliedColors.size > 1)
        assertEquals(255, hue.appliedColors.first().red)
        assertEquals(255, hue.appliedColors.first().green)
        assertEquals(255, hue.appliedColors.first().blue)
        val restored = hue.appliedColors.last()
        assertEquals(227, restored.red)
        assertEquals(155, restored.green)
        assertEquals(90, restored.blue)
        assertEquals(55, restored.brightness)
        assertTrue(restored.transitionDurationMs > 0)
    }

    @Test
    fun noTargetsWhenNothingSelected() = runTest {
        assertIs<PlayAtmosphereLightingEffectUseCase.Result.NoTargets>(
            useCase(AtmosphereSettingsStore(preferences))(
                effect = AtmosphereLightingEffect.CriticalStrike,
                currentLook = LightingLook(true, 80, 196, 30, 58),
            ),
        )
    }

    private fun useCase(store: AtmosphereSettingsStore): PlayAtmosphereLightingEffectUseCase {
        return PlayAtmosphereLightingEffectUseCase(
            ApplyAtmosphereLookUseCase(store, hue, govee),
        )
    }

    private companion object {
        const val TEST_NODE = "io.github.kmbisset89.worldweaver.test.atmosphere.playeffect"
    }
}

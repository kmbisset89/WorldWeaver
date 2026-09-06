package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class PlayAtmosphereLightingLoopUseCaseTest {
    private val preferences = Preferences.userRoot().node(TEST_NODE)
    private val hue = FakeHueClient()
    private val govee = FakeGoveeLightingClient()

    @AfterTest
    fun tearDown() {
        preferences.removeNode()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun fireKeepsApplyingUntilCancelled() = runTest {
        val store = AtmosphereSettingsStore(preferences)
        store.setHueConnection(HueConnection("192.168.1.40", "hue-key"))

        val job = launch {
            useCase(store)(
                loop = AtmosphereLightingLoop.Fire,
                hueLightIds = listOf("light-1"),
            )
        }
        advanceTimeBy(800)
        job.cancel()
        job.join()

        assertTrue(hue.appliedColors.size >= 3)
        hue.appliedColors.forEach { applied ->
            assertTrue(applied.red > applied.blue)
            assertEquals(AtmosphereLightingLoopCalculator.SAMPLE_INTERVAL_MS, applied.transitionDurationMs)
        }
    }

    @Test
    fun noTargetsWhenNothingSelected() = runTest {
        assertIs<PlayAtmosphereLightingLoopUseCase.Result.NoTargets>(
            useCase(AtmosphereSettingsStore(preferences))(
                loop = AtmosphereLightingLoop.Water,
            ),
        )
    }

    private fun useCase(store: AtmosphereSettingsStore): PlayAtmosphereLightingLoopUseCase {
        return PlayAtmosphereLightingLoopUseCase(
            ApplyAtmosphereLookUseCase(store, hue, govee),
        )
    }

    private companion object {
        const val TEST_NODE = "io.github.kmbisset89.worldweaver.test.atmosphere.playloop"
    }
}

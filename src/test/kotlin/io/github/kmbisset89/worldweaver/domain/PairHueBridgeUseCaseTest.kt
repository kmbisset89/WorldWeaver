package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class PairHueBridgeUseCaseTest {
    private val preferences = Preferences.userRoot().node(TEST_NODE)
    private val client = FakeHueClient()

    @AfterTest
    fun tearDown() {
        preferences.removeNode()
    }

    @Test
    fun storesApplicationKeyOnSuccess() = runTest {
        client.pairResult = HueClient.PairResult.Paired("hue-key")
        val store = AtmosphereSettingsStore(preferences)
        val useCase = PairHueBridgeUseCase(HueBridgeHostParser(), client, store)

        assertIs<PairHueBridgeUseCase.Result.Paired>(useCase("192.168.1.40", "WorldWeaver#host"))
        assertEquals("192.168.1.40", store.settings.value.hue.bridgeHost)
        assertEquals("hue-key", store.settings.value.hue.applicationKey)
        assertEquals("192.168.1.40", client.lastPairHost)
    }

    @Test
    fun reportsLinkButtonNotPressed() = runTest {
        client.pairResult = HueClient.PairResult.LinkButtonNotPressed
        val useCase = PairHueBridgeUseCase(
            HueBridgeHostParser(),
            client,
            AtmosphereSettingsStore(preferences),
        )
        assertIs<PairHueBridgeUseCase.Result.LinkButtonNotPressed>(
            useCase("192.168.1.40", "WorldWeaver#host"),
        )
    }

    private companion object {
        const val TEST_NODE = "io.github.kmbisset89.worldweaver.test.hue.pair"
    }
}

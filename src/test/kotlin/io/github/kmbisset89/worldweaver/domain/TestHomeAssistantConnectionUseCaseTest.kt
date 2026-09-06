package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class TestHomeAssistantConnectionUseCaseTest {
    private val parser = HomeAssistantConnectionParser()
    private val client = FakeHomeAssistantClient()
    private val useCase = TestHomeAssistantConnectionUseCase(parser, client)

    @Test
    fun connectedWhenClientSucceeds() = runTest {
        client.connectionResult = HomeAssistantClient.ConnectionResult.Connected

        val result = useCase("http://ha.local:8123", "token")

        assertIs<TestHomeAssistantConnectionUseCase.Result.Connected>(result)
        assertEquals("http://ha.local:8123", client.lastConnection?.baseUrl)
        assertEquals("token", client.lastConnection?.token)
    }

    @Test
    fun unauthorizedWhenTokenRejected() = runTest {
        client.connectionResult = HomeAssistantClient.ConnectionResult.Unauthorized
        assertIs<TestHomeAssistantConnectionUseCase.Result.Unauthorized>(
            useCase("http://ha.local:8123", "bad"),
        )
    }

    @Test
    fun unreachableWhenClientCannotReachHub() = runTest {
        client.connectionResult = HomeAssistantClient.ConnectionResult.Unreachable("Could not reach Home Assistant")
        val result = useCase("http://ha.local:8123", "token")
        val unreachable = assertIs<TestHomeAssistantConnectionUseCase.Result.Unreachable>(result)
        assertEquals("Could not reach Home Assistant", unreachable.message)
    }

    @Test
    fun invalidUrlNeverCallsClient() = runTest {
        assertIs<TestHomeAssistantConnectionUseCase.Result.InvalidUrl>(
            useCase("not-a-url", "token"),
        )
        assertEquals(null, client.lastConnection)
    }
}

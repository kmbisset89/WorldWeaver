package io.github.kmbisset89.worldweaver.domain

import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class SaveHomeAssistantConnectionUseCaseTest {
    private val preferences = Preferences.userRoot().node(TEST_NODE)

    @AfterTest
    fun tearDown() {
        preferences.removeNode()
    }

    @Test
    fun persistsNormalizedConnection() {
        val store = AtmosphereSettingsStore(preferences)
        val useCase = SaveHomeAssistantConnectionUseCase(HomeAssistantConnectionParser(), store)

        val result = useCase("http://ha.local:8123/", " token ")

        assertIs<SaveHomeAssistantConnectionUseCase.Result.Saved>(result)
        assertEquals("http://ha.local:8123", store.settings.value.connection.baseUrl)
        assertEquals("token", store.settings.value.connection.token)
    }

    @Test
    fun rejectsInvalidUrl() {
        val store = AtmosphereSettingsStore(preferences)
        val useCase = SaveHomeAssistantConnectionUseCase(HomeAssistantConnectionParser(), store)

        assertIs<SaveHomeAssistantConnectionUseCase.Result.InvalidUrl>(
            useCase("not-a-url", "token"),
        )
        assertEquals("", store.settings.value.connection.baseUrl)
    }

    private companion object {
        const val TEST_NODE = "io.github.kmbisset89.worldweaver.test.atmosphere.save"
    }
}

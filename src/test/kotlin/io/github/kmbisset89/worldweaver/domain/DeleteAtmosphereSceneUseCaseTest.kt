package io.github.kmbisset89.worldweaver.domain

import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class DeleteAtmosphereSceneUseCaseTest {
    private val preferences = Preferences.userRoot().node(TEST_NODE)

    @AfterTest
    fun tearDown() {
        preferences.removeNode()
    }

    @Test
    fun deletesExistingScene() {
        val store = AtmosphereSettingsStore(preferences)
        store.setScenes(
            listOf(AtmosphereScene("s1", "Tavern", "scene.tavern", 0)),
        )
        val useCase = DeleteAtmosphereSceneUseCase(store)

        assertIs<DeleteAtmosphereSceneUseCase.Result.Deleted>(useCase("s1"))
        assertEquals(emptyList(), store.settings.value.scenes)
    }

    @Test
    fun missingSceneIsNotFound() {
        val useCase = DeleteAtmosphereSceneUseCase(AtmosphereSettingsStore(preferences))
        assertIs<DeleteAtmosphereSceneUseCase.Result.NotFound>(useCase("missing"))
    }

    private companion object {
        const val TEST_NODE = "io.github.kmbisset89.worldweaver.test.atmosphere.delete"
    }
}

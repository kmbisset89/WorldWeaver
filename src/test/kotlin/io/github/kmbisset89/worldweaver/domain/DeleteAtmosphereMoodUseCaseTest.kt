package io.github.kmbisset89.worldweaver.domain

import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class DeleteAtmosphereMoodUseCaseTest {
    private val preferences = Preferences.userRoot().node(TEST_NODE)

    @AfterTest
    fun tearDown() {
        preferences.removeNode()
    }

    @Test
    fun deletesExistingMood() {
        val store = AtmosphereSettingsStore(preferences)
        store.setMoods(
            listOf(AtmosphereMood("m1", "Temple", true, 40, "#C4A35A", 0)),
        )
        val useCase = DeleteAtmosphereMoodUseCase(store)

        assertIs<DeleteAtmosphereMoodUseCase.Result.Deleted>(useCase("m1"))
        assertEquals(emptyList(), store.settings.value.moods)
    }

    @Test
    fun missingMoodIsNotFound() {
        val useCase = DeleteAtmosphereMoodUseCase(AtmosphereSettingsStore(preferences))
        assertIs<DeleteAtmosphereMoodUseCase.Result.NotFound>(useCase("missing"))
    }

    private companion object {
        const val TEST_NODE = "io.github.kmbisset89.worldweaver.test.atmosphere.mood.delete"
    }
}

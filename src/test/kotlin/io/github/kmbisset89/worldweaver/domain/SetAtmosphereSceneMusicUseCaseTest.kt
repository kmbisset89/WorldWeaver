package io.github.kmbisset89.worldweaver.domain

import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class SetAtmosphereSceneMusicUseCaseTest {
    private val preferences = Preferences.userRoot().node(TEST_NODE)

    @AfterTest
    fun tearDown() {
        preferences.removeNode()
    }

    @Test
    fun attachesAndClearsTrack() {
        val store = AtmosphereSettingsStore(preferences)
        store.setMusicTracks(
            listOf(AtmosphereMusicTrack("t1", "Tavern", "/tmp/tavern.mp3", 0)),
        )
        store.setScenes(
            listOf(AtmosphereScene("s1", "Tavern", "scene.tavern", 0)),
        )
        val useCase = SetAtmosphereSceneMusicUseCase(store)

        assertIs<SetAtmosphereSceneMusicUseCase.Result.Updated>(useCase("s1", "t1"))
        assertEquals("t1", store.settings.value.scenes.single().musicTrackId)

        assertIs<SetAtmosphereSceneMusicUseCase.Result.Updated>(useCase("s1", null))
        assertEquals("", store.settings.value.scenes.single().musicTrackId)
    }

    @Test
    fun rejectsUnknownSceneOrTrack() {
        val store = AtmosphereSettingsStore(preferences)
        store.setMusicTracks(
            listOf(AtmosphereMusicTrack("t1", "Tavern", "/tmp/tavern.mp3", 0)),
        )
        store.setScenes(
            listOf(AtmosphereScene("s1", "Tavern", "scene.tavern", 0)),
        )
        val useCase = SetAtmosphereSceneMusicUseCase(store)
        assertIs<SetAtmosphereSceneMusicUseCase.Result.SceneNotFound>(useCase("missing", "t1"))
        assertIs<SetAtmosphereSceneMusicUseCase.Result.TrackNotFound>(useCase("s1", "missing"))
    }

    private companion object {
        const val TEST_NODE = "io.github.kmbisset89.worldweaver.test.atmosphere.scene.music"
    }
}

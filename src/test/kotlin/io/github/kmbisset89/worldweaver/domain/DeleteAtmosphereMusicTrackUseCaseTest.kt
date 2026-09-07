package io.github.kmbisset89.worldweaver.domain

import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class DeleteAtmosphereMusicTrackUseCaseTest {
    private val preferences = Preferences.userRoot().node(TEST_NODE)

    @AfterTest
    fun tearDown() {
        preferences.removeNode()
    }

    @Test
    fun deletesTrackAndClearsSceneLinks() {
        val store = AtmosphereSettingsStore(preferences)
        store.setMusicTracks(
            listOf(
                AtmosphereMusicTrack("t1", "Tavern", "/tmp/tavern.mp3", 0),
                AtmosphereMusicTrack("t2", "Combat", "/tmp/combat.mp3", 1),
            ),
        )
        store.setScenes(
            listOf(
                AtmosphereScene(
                    id = "s1",
                    name = "Tavern",
                    entityId = "scene.tavern",
                    sortOrder = 0,
                    musicTrackId = "t1",
                ),
                AtmosphereScene(
                    id = "s2",
                    name = "Combat",
                    entityId = "scene.combat",
                    sortOrder = 1,
                    musicTrackId = "t2",
                ),
            ),
        )
        val useCase = DeleteAtmosphereMusicTrackUseCase(store)

        assertIs<DeleteAtmosphereMusicTrackUseCase.Result.Deleted>(useCase("t1"))
        assertEquals(listOf("t2"), store.settings.value.musicTracks.map { it.id })
        assertEquals("", store.settings.value.scenes.first { it.id == "s1" }.musicTrackId)
        assertEquals("t2", store.settings.value.scenes.first { it.id == "s2" }.musicTrackId)
    }

    @Test
    fun missingTrackIsNotFound() {
        val useCase = DeleteAtmosphereMusicTrackUseCase(AtmosphereSettingsStore(preferences))
        assertIs<DeleteAtmosphereMusicTrackUseCase.Result.NotFound>(useCase("missing"))
    }

    private companion object {
        const val TEST_NODE = "io.github.kmbisset89.worldweaver.test.atmosphere.music.delete"
    }
}

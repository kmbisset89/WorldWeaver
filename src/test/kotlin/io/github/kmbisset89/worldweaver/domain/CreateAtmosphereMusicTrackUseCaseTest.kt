package io.github.kmbisset89.worldweaver.domain

import java.io.File
import java.nio.file.Files
import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class CreateAtmosphereMusicTrackUseCaseTest {
    private val preferences = Preferences.userRoot().node(TEST_NODE)
    private val tempDir = Files.createTempDirectory("ww-atmosphere-music-create").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
        preferences.removeNode()
    }

    @Test
    fun createsLinkedTrackFromExistingFile() {
        val file = File(tempDir, "tavern.mp3").also { it.writeBytes(byteArrayOf(1, 2, 3)) }
        val store = AtmosphereSettingsStore(preferences)
        val useCase = CreateAtmosphereMusicTrackUseCase(store, EntityIdFactory { "track-1" })

        val created = assertIs<CreateAtmosphereMusicTrackUseCase.Result.Created>(useCase(file.absolutePath))

        assertEquals("track-1", created.track.id)
        assertEquals("tavern", created.track.displayName)
        assertEquals(file.canonicalFile.absolutePath, created.track.path)
        assertEquals(0, created.track.sortOrder)
        assertEquals(listOf(created.track), store.settings.value.musicTracks)
    }

    @Test
    fun rejectsMissingAndUnsupportedFiles() {
        val store = AtmosphereSettingsStore(preferences)
        val useCase = CreateAtmosphereMusicTrackUseCase(store, EntityIdFactory { "track-1" })
        assertIs<CreateAtmosphereMusicTrackUseCase.Result.InvalidPath>(useCase("  "))
        assertIs<CreateAtmosphereMusicTrackUseCase.Result.InvalidPath>(
            useCase(File(tempDir, "missing.mp3").absolutePath),
        )
        val text = File(tempDir, "notes.txt").also { it.writeText("nope") }
        assertIs<CreateAtmosphereMusicTrackUseCase.Result.UnsupportedFormat>(useCase(text.absolutePath))
    }

    @Test
    fun rejectsDuplicateCanonicalPath() {
        val file = File(tempDir, "tavern.wav").also { it.writeBytes(byteArrayOf(1)) }
        val store = AtmosphereSettingsStore(preferences)
        val useCase = CreateAtmosphereMusicTrackUseCase(store, EntityIdFactory { "track-2" })
        assertIs<CreateAtmosphereMusicTrackUseCase.Result.Created>(useCase(file.absolutePath))
        assertIs<CreateAtmosphereMusicTrackUseCase.Result.DuplicatePath>(useCase(file.absolutePath))
        assertEquals(1, store.settings.value.musicTracks.size)
    }

    @Test
    fun incrementsSortOrder() {
        val store = AtmosphereSettingsStore(preferences)
        store.setMusicTracks(
            listOf(AtmosphereMusicTrack("existing", "Combat", "/tmp/combat.mp3", 3)),
        )
        val file = File(tempDir, "tavern.ogg").also { it.writeBytes(byteArrayOf(1)) }
        val useCase = CreateAtmosphereMusicTrackUseCase(store, EntityIdFactory { "track-2" })
        val created = assertIs<CreateAtmosphereMusicTrackUseCase.Result.Created>(useCase(file.absolutePath))
        assertEquals(4, created.track.sortOrder)
    }

    private companion object {
        const val TEST_NODE = "io.github.kmbisset89.worldweaver.test.atmosphere.music.create"
    }
}

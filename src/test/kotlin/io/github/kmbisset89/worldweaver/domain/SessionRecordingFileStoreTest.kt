package io.github.kmbisset89.worldweaver.domain

import java.nio.file.Files
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SessionRecordingFileStoreTest {
    @Test
    fun createListAndDeleteRoundTrip() {
        val store = SessionRecordingFileStore(Files.createTempDirectory("ww-recordings").toFile())
        val startedAt = Instant.parse("2026-09-06T15:48:01Z")
        val file = store.createFile("session-1", SessionRecordingKind.Audio, startedAt)
        file.writeBytes(ByteArray(40))

        val listed = store.list("session-1")
        assertEquals(1, listed.size)
        assertEquals(SessionRecordingKind.Audio, listed.single().kind)
        assertEquals(startedAt, listed.single().startedAt)
        assertEquals(40L, listed.single().byteSize)
        assertTrue(listed.single().id.endsWith("_audio.wav"))

        assertTrue(store.delete("session-1", listed.single().id))
        assertTrue(store.list("session-1").isEmpty())
    }

    @Test
    fun deleteAllRemovesTheSessionFolder() {
        val store = SessionRecordingFileStore(Files.createTempDirectory("ww-recordings").toFile())
        val audio = store.createFile("session-1", SessionRecordingKind.Audio, Instant.parse("2026-09-06T15:00:00Z"))
        val video = store.createFile("session-1", SessionRecordingKind.Video, Instant.parse("2026-09-06T15:01:00Z"))
        audio.writeBytes(ByteArray(8))
        video.writeBytes(ByteArray(12))
        val other = store.createFile("session-2", SessionRecordingKind.Audio, Instant.parse("2026-09-06T15:02:00Z"))
        other.writeBytes(ByteArray(4))

        store.deleteAll("session-1")

        assertTrue(store.list("session-1").isEmpty())
        assertEquals(1, store.list("session-2").size)
        assertFalse(audio.exists())
        assertTrue(other.exists())
    }

    @Test
    fun listIgnoresUnknownFiles() {
        val store = SessionRecordingFileStore(Files.createTempDirectory("ww-recordings").toFile())
        val file = store.createFile("session-1", SessionRecordingKind.Audio, Instant.parse("2026-09-06T15:00:00Z"))
        file.writeBytes(ByteArray(6))
        java.io.File(file.parentFile, "notes.txt").writeText("scratch")

        assertEquals(1, store.list("session-1").size)
    }
}

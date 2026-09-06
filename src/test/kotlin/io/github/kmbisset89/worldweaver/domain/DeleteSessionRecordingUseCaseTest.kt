package io.github.kmbisset89.worldweaver.domain

import java.nio.file.Files
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class DeleteSessionRecordingUseCaseTest {
    @Test
    fun deleteRemovesTheFile() {
        val store = SessionRecordingFileStore(Files.createTempDirectory("ww-recordings").toFile())
        val file = store.createFile("session-1", SessionRecordingKind.Audio, Instant.parse("2026-09-06T15:00:00Z"))
        file.writeBytes(ByteArray(10))
        val recording = store.list("session-1").single()
        val useCase = DeleteSessionRecordingUseCase(store)

        val result = useCase("session-1", recording.id)

        assertIs<DeleteSessionRecordingUseCase.Result.Deleted>(result)
        assertTrue(store.list("session-1").isEmpty())
    }

    @Test
    fun missingFileIsNotFound() {
        val store = SessionRecordingFileStore(Files.createTempDirectory("ww-recordings").toFile())
        val useCase = DeleteSessionRecordingUseCase(store)

        val result = useCase("session-1", "missing.wav")

        assertEquals(DeleteSessionRecordingUseCase.Result.NotFound, result)
    }
}

package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class UpdateSessionRunnerNotesUseCaseTest {
    @Test
    fun updatesNotesAndScratchWithoutTouchingScenes() = runTest {
        val harness = Harness()
        harness.insertSession()

        val result = harness.updateNotes("session-1", "  Prep  ", "  Live scratch  ")

        assertIs<UpdateSessionRunnerNotesUseCase.Result.Updated>(result)
        val session = harness.sessions.getById("session-1")!!
        assertEquals("Prep", session.notes)
        assertEquals("Live scratch", session.scratchNotes)
        assertEquals(listOf("Arrival"), session.scenes.map { it.title })
        assertEquals(Instant.parse("2026-08-29T13:00:00Z"), session.updatedAt)
    }

    @Test
    fun returnsNotFoundForUnknownSession() = runTest {
        val harness = Harness()

        val result = harness.updateNotes("missing", "notes", "scratch")

        assertIs<UpdateSessionRunnerNotesUseCase.Result.NotFound>(result)
    }

    private class Harness {
        val sessions = FakeSessionRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T13:00:00Z") }
        private val updateNotes = UpdateSessionRunnerNotesUseCase(sessions, instant)

        suspend fun updateNotes(sessionId: String, notes: String, scratchNotes: String) =
            updateNotes.invoke(sessionId, notes, scratchNotes)

        suspend fun insertSession() {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            sessions.insert(
                Session(
                    id = "session-1",
                    campaignId = "campaign-1",
                    name = "Tonight",
                    notes = "Old",
                    scratchNotes = "Old scratch",
                    scenes = listOf(SessionScene(id = "scene-1", title = "Arrival", notes = "")),
                    marchOrder = emptyList(),
                    createdAt = now,
                    updatedAt = now,
                )
            )
        }
    }
}

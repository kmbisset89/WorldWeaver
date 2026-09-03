package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

internal class CreateReferenceDocUseCaseTest {
    @Test
    fun createStoresPathAsGiven() = runTest {
        val harness = Harness()
        harness.context.setActiveCampaignId("campaign-1")

        val result = harness.createDoc(
            ReferenceDocDraft(
                sessionId = null,
                title = "DM notes",
                pathOrUrl = "  /Users/dm/notes.pdf  ",
            )
        )

        val created = assertIs<CreateReferenceDocUseCase.Result.Created>(result)
        assertEquals("DM notes", created.doc.title)
        assertEquals("/Users/dm/notes.pdf", created.doc.pathOrUrl)
        assertNull(created.doc.sessionId)
    }

    @Test
    fun createRejectsBlankPath() = runTest {
        val harness = Harness()
        harness.context.setActiveCampaignId("campaign-1")

        val result = harness.createDoc(
            ReferenceDocDraft(
                sessionId = null,
                title = "Notes",
                pathOrUrl = " ",
            )
        )

        assertIs<CreateReferenceDocUseCase.Result.InvalidPath>(result)
    }

    private class Harness {
        val docs = FakeReferenceDocRepository()
        val sessions = FakeSessionRepository()
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        private val ids = EntityIdFactory { "doc-1" }
        val createDoc = CreateReferenceDocUseCase(docs, sessions, context, ids, instant)
    }
}

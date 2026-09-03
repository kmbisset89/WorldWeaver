package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

internal class CreatePlotThreadUseCaseTest {
    @Test
    fun createAllowsNullSession() = runTest {
        val harness = Harness()
        harness.context.setActiveCampaignId("campaign-1")

        val result = harness.createThread(
            PlotThreadDraft(
                sessionId = null,
                title = "Who hired the bandits?",
                details = "Ask the magistrate.",
                status = PlotThreadStatus.Open,
                priority = PlotThreadPriority.High,
            )
        )

        val created = assertIs<CreatePlotThreadUseCase.Result.Created>(result)
        assertEquals("campaign-1", created.thread.campaignId)
        assertNull(created.thread.sessionId)
        assertEquals(PlotThreadPriority.High, created.thread.priority)
    }

    @Test
    fun createRejectsBlankTitle() = runTest {
        val harness = Harness()
        harness.context.setActiveCampaignId("campaign-1")

        val result = harness.createThread(
            PlotThreadDraft(
                sessionId = null,
                title = " ",
                details = "",
                status = PlotThreadStatus.Open,
                priority = PlotThreadPriority.Medium,
            )
        )

        assertIs<CreatePlotThreadUseCase.Result.InvalidTitle>(result)
    }

    private class Harness {
        val threads = FakePlotThreadRepository()
        val sessions = FakeSessionRepository()
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        private val ids = EntityIdFactory { "thread-1" }
        val createThread = CreatePlotThreadUseCase(threads, sessions, context, ids, instant)
    }
}

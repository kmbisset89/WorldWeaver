package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class CreateSessionClockUseCaseTest {
    @Test
    fun createsClockOnTheActiveSession() = runTest {
        val harness = Harness()
        harness.insertSession()
        harness.context.setActiveSessionId("session-1")

        val result = harness.createClock("The ritual", 8)

        val created = assertIs<CreateSessionClockUseCase.Result.Created>(result)
        assertEquals("session-1", created.clock.sessionId)
        assertEquals("The ritual", created.clock.label)
        assertEquals(8, created.clock.segmentCount)
        assertEquals(0, created.clock.filledCount)
        assertEquals(0, created.clock.sortIndex)
    }

    @Test
    fun rejectsBlankLabelAndOutOfRangeSegments() = runTest {
        val harness = Harness()
        harness.insertSession()
        harness.context.setActiveSessionId("session-1")

        assertIs<CreateSessionClockUseCase.Result.InvalidLabel>(harness.createClock("  ", 6))
        assertIs<CreateSessionClockUseCase.Result.InvalidSegmentCount>(harness.createClock("Watch", 1))
        assertIs<CreateSessionClockUseCase.Result.InvalidSegmentCount>(harness.createClock("Watch", 13))
    }

    @Test
    fun requiresAnActiveSession() = runTest {
        val harness = Harness()

        val result = harness.createClock("The ritual", 6)

        assertIs<CreateSessionClockUseCase.Result.NoActiveSession>(result)
    }

    private class Harness {
        val clocks = FakeSessionClockRepository()
        val sessions = FakeSessionRepository()
        val context = FakeActiveContextRepository()
        private val ids = EntityIdFactory { "clock-1" }
        private val createClock = CreateSessionClockUseCase(clocks, sessions, context, ids)

        suspend fun createClock(label: String, segmentCount: Int) = createClock.invoke(label, segmentCount)

        suspend fun insertSession() {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            sessions.insert(
                Session(
                    id = "session-1",
                    campaignId = "campaign-1",
                    name = "Tonight",
                    notes = "",
                    scenes = emptyList(),
                    marchOrder = emptyList(),
                    createdAt = now,
                    updatedAt = now,
                )
            )
        }
    }
}

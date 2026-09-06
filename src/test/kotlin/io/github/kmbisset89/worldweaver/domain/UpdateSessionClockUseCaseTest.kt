package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class UpdateSessionClockUseCaseTest {
    @Test
    fun clampsFilledCountToTheClock() = runTest {
        val harness = Harness()
        harness.clocks.insert(clock(filledCount = 1))

        val result = harness.updateClock("clock-1", 9)

        assertIs<UpdateSessionClockUseCase.Result.Updated>(result)
        assertEquals(6, harness.clocks.getById("clock-1")?.filledCount)
    }

    @Test
    fun returnsNotFoundForUnknownClock() = runTest {
        val harness = Harness()

        val result = harness.updateClock("missing", 2)

        assertIs<UpdateSessionClockUseCase.Result.NotFound>(result)
    }

    private class Harness {
        val clocks = FakeSessionClockRepository()
        private val updateClock = UpdateSessionClockUseCase(clocks)

        suspend fun updateClock(clockId: String, filledCount: Int) = updateClock.invoke(clockId, filledCount)
    }

    private fun clock(filledCount: Int): SessionClock {
        return SessionClock(
            id = "clock-1",
            sessionId = "session-1",
            label = "The ritual",
            segmentCount = 6,
            filledCount = filledCount,
            sortIndex = 0,
        )
    }
}

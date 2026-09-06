package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNull

internal class DeleteSessionClockUseCaseTest {
    @Test
    fun deletesAnExistingClock() = runTest {
        val harness = Harness()
        harness.clocks.insert(
            SessionClock(
                id = "clock-1",
                sessionId = "session-1",
                label = "The ritual",
                segmentCount = 6,
                filledCount = 2,
                sortIndex = 0,
            )
        )

        val result = harness.deleteClock("clock-1")

        assertIs<DeleteSessionClockUseCase.Result.Deleted>(result)
        assertNull(harness.clocks.getById("clock-1"))
    }

    private class Harness {
        val clocks = FakeSessionClockRepository()
        private val deleteClock = DeleteSessionClockUseCase(clocks)

        suspend fun deleteClock(clockId: String) = deleteClock.invoke(clockId)
    }
}

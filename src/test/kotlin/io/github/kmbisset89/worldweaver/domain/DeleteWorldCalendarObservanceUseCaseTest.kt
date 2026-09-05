package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class DeleteWorldCalendarObservanceUseCaseTest {
    @Test
    fun deleteRemovesObservance() = runTest {
        val observances = FakeWorldCalendarObservanceRepository()
        val now = Instant.parse("2026-09-04T12:00:00Z")
        observances.insert(
            WorldCalendarObservance(
                id = "obs-1",
                worldId = "world-1",
                name = "Midwinter",
                notes = "",
                kind = WorldCalendarObservanceKind.Holiday,
                monthId = "m-1",
                day = 1,
                year = null,
                loreIds = emptyList(),
                createdAt = now,
                updatedAt = now,
            )
        )
        val deleteObservance = DeleteWorldCalendarObservanceUseCase(observances)

        val result = deleteObservance("obs-1")

        assertIs<DeleteWorldCalendarObservanceUseCase.Result.Deleted>(result)
        assertTrue(observances.all().isEmpty())
    }

    @Test
    fun deleteReportsMissingObservance() = runTest {
        val deleteObservance = DeleteWorldCalendarObservanceUseCase(FakeWorldCalendarObservanceRepository())

        val result = deleteObservance("missing")

        assertIs<DeleteWorldCalendarObservanceUseCase.Result.NotFound>(result)
    }
}

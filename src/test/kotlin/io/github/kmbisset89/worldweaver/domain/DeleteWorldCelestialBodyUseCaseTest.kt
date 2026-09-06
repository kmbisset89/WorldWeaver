package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class DeleteWorldCelestialBodyUseCaseTest {
    @Test
    fun deleteRemovesBody() = runTest {
        val bodies = FakeWorldCelestialBodyRepository()
        val now = Instant.parse("2026-09-05T12:00:00Z")
        bodies.insert(
            WorldCelestialBody(
                id = "body-1",
                worldId = "world-1",
                name = "The Moon",
                notes = "",
                kind = CelestialBodyKind.Moon,
                periodDays = 29,
                epochOffsetDays = 0,
                sortIndex = 0,
                createdAt = now,
                updatedAt = now,
            )
        )
        val deleteBody = DeleteWorldCelestialBodyUseCase(bodies)

        val result = deleteBody("body-1")

        assertIs<DeleteWorldCelestialBodyUseCase.Result.Deleted>(result)
        assertTrue(bodies.all().isEmpty())
    }

    @Test
    fun deleteReportsMissingBody() = runTest {
        val deleteBody = DeleteWorldCelestialBodyUseCase(FakeWorldCelestialBodyRepository())

        val result = deleteBody("missing")

        assertIs<DeleteWorldCelestialBodyUseCase.Result.NotFound>(result)
    }
}

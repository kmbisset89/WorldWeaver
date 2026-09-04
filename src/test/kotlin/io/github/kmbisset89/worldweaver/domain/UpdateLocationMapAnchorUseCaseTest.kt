package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

internal class UpdateLocationMapAnchorUseCaseTest {
    @Test
    fun placesAndClearsNormalizedAnchors() = runTest {
        val harness = Harness()
        val location = harness.insertContinent()

        val placed = harness.updateAnchor(location.id, 0.25, 0.75)
        assertIs<UpdateLocationMapAnchorUseCase.Result.Updated>(placed)
        val stored = harness.locations.getById(location.id)
        assertEquals(0.25, stored?.mapAnchorX)
        assertEquals(0.75, stored?.mapAnchorY)

        val cleared = harness.updateAnchor(location.id, null, null)
        assertIs<UpdateLocationMapAnchorUseCase.Result.Updated>(cleared)
        val clearedLocation = harness.locations.getById(location.id)
        assertNull(clearedLocation?.mapAnchorX)
        assertNull(clearedLocation?.mapAnchorY)
    }

    @Test
    fun rejectsPartialOrOutOfRangeAnchors() = runTest {
        val harness = Harness()
        val location = harness.insertContinent()

        assertIs<UpdateLocationMapAnchorUseCase.Result.InvalidAnchor>(
            harness.updateAnchor(location.id, 0.5, null)
        )
        assertIs<UpdateLocationMapAnchorUseCase.Result.InvalidAnchor>(
            harness.updateAnchor(location.id, 1.5, 0.2)
        )
    }

    private class Harness {
        val locations = FakeLocationRepository()
        private val instant = InstantProvider { Instant.parse("2026-09-04T12:00:00Z") }
        val updateLocationMapAnchor = UpdateLocationMapAnchorUseCase(locations, instant)

        suspend fun insertContinent(): Location {
            val now = Instant.parse("2026-09-04T12:00:00Z")
            val location = Location(
                id = "loc-1",
                worldId = "world-1",
                type = LocationType.Continent,
                parentLocationId = null,
                name = "Faerun",
                description = "",
                climate = "",
                terrain = "",
                government = "",
                landmarks = emptyList(),
                history = "",
                notes = "",
                createdAt = now,
                updatedAt = now,
            )
            locations.insert(location)
            return location
        }

        suspend fun updateAnchor(
            locationId: String,
            x: Double?,
            y: Double?,
        ): UpdateLocationMapAnchorUseCase.Result {
            return updateLocationMapAnchor(locationId, x, y)
        }
    }
}

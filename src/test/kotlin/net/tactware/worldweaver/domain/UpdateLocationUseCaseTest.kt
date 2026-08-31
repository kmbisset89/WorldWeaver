package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class UpdateLocationUseCaseTest {
    @Test
    fun updateRejectsEmptyName() = runTest {
        val harness = Harness()
        val location = harness.insertContinent("Faerun")

        val result = harness.updateLocation(
            location.id,
            harness.draft(name = "  "),
        )

        assertIs<UpdateLocationUseCase.Result.InvalidName>(result)
        assertEquals("Faerun", harness.locations.getById(location.id)?.name)
    }

    @Test
    fun updatePersistsMetadataAndLandmarks() = runTest {
        val harness = Harness()
        val location = harness.insertContinent("Faerun")

        val result = harness.updateLocation(
            location.id,
            harness.draft(
                name = "Faerûn",
                climate = "varied",
                terrain = "mixed",
                government = "many",
                landmarks = listOf("Waterdeep", "Neverwinter"),
                history = "Long",
                notes = "Setting notes",
            ),
        )

        assertIs<UpdateLocationUseCase.Result.Updated>(result)
        val updated = harness.locations.getById(location.id)
        assertEquals("Faerûn", updated?.name)
        assertEquals("varied", updated?.climate)
        assertEquals(listOf("Waterdeep", "Neverwinter"), updated?.landmarks)
        assertEquals("Setting notes", updated?.notes)
    }

    @Test
    fun updateRejectsInvalidParent() = runTest {
        val harness = Harness()
        val continent = harness.insertContinent("Faerun")

        val result = harness.updateLocation(
            continent.id,
            harness.draft(parentLocationId = continent.id),
        )

        assertIs<UpdateLocationUseCase.Result.InvalidParent>(result)
    }

    private class Harness {
        val locations = FakeLocationRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T13:00:00Z") }
        val updateLocation = UpdateLocationUseCase(locations, instant)

        suspend fun insertContinent(name: String): Location {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            val location = Location(
                id = "loc-1",
                worldId = "world-1",
                type = LocationType.Continent,
                parentLocationId = null,
                name = name,
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

        fun draft(
            name: String = "Faerun",
            parentLocationId: String? = null,
            climate: String = "",
            terrain: String = "",
            government: String = "",
            landmarks: List<String> = emptyList(),
            history: String = "",
            notes: String = "",
        ): LocationDraft {
            return LocationDraft(
                type = LocationType.Continent,
                parentLocationId = parentLocationId,
                name = name,
                description = "",
                climate = climate,
                terrain = terrain,
                government = government,
                landmarks = landmarks,
                history = history,
                notes = notes,
            )
        }
    }
}

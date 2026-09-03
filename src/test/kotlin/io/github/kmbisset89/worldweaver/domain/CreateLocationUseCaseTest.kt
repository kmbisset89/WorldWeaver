package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class CreateLocationUseCaseTest {
    @Test
    fun createRequiresActiveWorld() = runTest {
        val harness = Harness()

        val result = harness.createLocation(continentDraft("Faerun"))

        assertIs<CreateLocationUseCase.Result.NoActiveWorld>(result)
        assertTrue(harness.locations.all().isEmpty())
    }

    @Test
    fun createRejectsBlankName() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")

        val result = harness.createLocation(continentDraft("  "))

        assertIs<CreateLocationUseCase.Result.InvalidName>(result)
    }

    @Test
    fun createContinentStoresWorldIdAndNoParent() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")

        val result = harness.createLocation(continentDraft("Faerun"))

        val created = assertIs<CreateLocationUseCase.Result.Created>(result)
        assertEquals("world-1", created.location.worldId)
        assertEquals(LocationType.Continent, created.location.type)
        assertNull(created.location.parentLocationId)
        assertEquals("Faerun", created.location.name)
    }

    @Test
    fun createRejectsCityUnderPlace() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")
        val place = harness.insertLocation(
            type = LocationType.Place,
            name = "Tavern",
        )

        val result = harness.createLocation(
            LocationDraft(
                type = LocationType.City,
                parentLocationId = place.id,
                name = "Waterdeep",
                description = "",
                climate = "",
                terrain = "",
                government = "",
                landmarks = emptyList(),
                history = "",
                notes = "",
            )
        )

        assertIs<CreateLocationUseCase.Result.InvalidParent>(result)
        assertEquals(1, harness.locations.all().size)
    }

    @Test
    fun createAreaUnderContinentSucceeds() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")
        val continent = harness.insertLocation(
            type = LocationType.Continent,
            name = "Faerun",
        )

        val result = harness.createLocation(
            LocationDraft(
                type = LocationType.Area,
                parentLocationId = continent.id,
                name = "Sword Coast",
                description = "West",
                climate = "temperate",
                terrain = "coast",
                government = "",
                landmarks = listOf("Candlekeep", "  "),
                history = "Old",
                notes = "World notes",
            )
        )

        val created = assertIs<CreateLocationUseCase.Result.Created>(result)
        assertEquals(continent.id, created.location.parentLocationId)
        assertEquals(listOf("Candlekeep"), created.location.landmarks)
        assertEquals("temperate", created.location.climate)
    }

    @Test
    fun createContinentRejectsParent() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")
        val continent = harness.insertLocation(
            type = LocationType.Continent,
            name = "Faerun",
        )

        val result = harness.createLocation(
            continentDraft("Kara-Tur").copy(parentLocationId = continent.id)
        )

        assertIs<CreateLocationUseCase.Result.InvalidParent>(result)
    }

    private class Harness {
        val locations = FakeLocationRepository()
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        private var nextId = 0
        private val ids = EntityIdFactory { "location-${++nextId}" }
        val createLocation = CreateLocationUseCase(locations, context, ids, instant)

        suspend fun insertLocation(
            type: LocationType,
            name: String,
            parentLocationId: String? = null,
        ): Location {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            val location = Location(
                id = "seed-${name.lowercase()}",
                worldId = "world-1",
                type = type,
                parentLocationId = parentLocationId,
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
    }

    private fun continentDraft(name: String): LocationDraft {
        return LocationDraft(
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
        )
    }
}

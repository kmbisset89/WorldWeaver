package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class DeleteLocationUseCaseTest {
    @Test
    fun deleteIsBlockedWhenChildrenExist() = runTest {
        val harness = Harness()
        val continent = harness.insert("loc-1", LocationType.Continent, "Faerun", null)
        harness.insert("loc-2", LocationType.Area, "Sword Coast", continent.id)

        val result = harness.deleteLocation(continent.id)

        val blocked = assertIs<DeleteLocationUseCase.Result.Blocked>(result)
        assertEquals(1, blocked.childCount)
        assertEquals(2, harness.locations.all().size)
    }

    @Test
    fun deleteRemovesLeafAndOverlays() = runTest {
        val harness = Harness()
        val place = harness.insert("loc-1", LocationType.Place, "Tavern", null)
        harness.overlays.upsert(
            LocationOverlay(
                campaignId = "campaign-1",
                locationId = place.id,
                hasPartyPresence = true,
                notes = "Camped here",
                updatedAt = Instant.parse("2026-08-29T12:00:00Z"),
            )
        )

        val result = harness.deleteLocation(place.id)

        assertIs<DeleteLocationUseCase.Result.Deleted>(result)
        assertTrue(harness.locations.all().isEmpty())
        assertTrue(harness.overlays.all().isEmpty())
    }

    private class Harness {
        val locations = FakeLocationRepository()
        val overlays = FakeLocationOverlayRepository()
        val deleteLocation = DeleteLocationUseCase(
            locations,
            overlays,
            VoiceClipFileStore(java.nio.file.Files.createTempDirectory("ww-voices").toFile()),
        )

        suspend fun insert(
            id: String,
            type: LocationType,
            name: String,
            parentLocationId: String?,
        ): Location {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            val location = Location(
                id = id,
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
}

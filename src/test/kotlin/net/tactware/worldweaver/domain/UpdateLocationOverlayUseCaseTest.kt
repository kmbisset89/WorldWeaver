package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class UpdateLocationOverlayUseCaseTest {
    @Test
    fun overlayRequiresActiveCampaign() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")
        val location = harness.insertLocation()

        val result = harness.updateOverlay(location.id, hasPartyPresence = true, notes = "Here")

        assertIs<UpdateLocationOverlayUseCase.Result.NoActiveCampaign>(result)
        assertTrue(harness.overlays.all().isEmpty())
    }

    @Test
    fun overlayDoesNotChangeWorldLocation() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")
        harness.context.setActiveCampaignId("campaign-1")
        val location = harness.insertLocation()

        val result = harness.updateOverlay(location.id, hasPartyPresence = true, notes = "Camped")

        assertIs<UpdateLocationOverlayUseCase.Result.Updated>(result)
        val overlay = harness.overlays.get("campaign-1", location.id)
        assertEquals(true, overlay?.hasPartyPresence)
        assertEquals("Camped", overlay?.notes)
        assertEquals("World notes", harness.locations.getById(location.id)?.notes)
    }

    @Test
    fun overlayIsScopedToCampaign() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")
        harness.context.setActiveCampaignId("campaign-1")
        val location = harness.insertLocation()
        harness.updateOverlay(location.id, hasPartyPresence = true, notes = "Party A")

        val other = harness.overlays.get("campaign-2", location.id)

        assertEquals(null, other)
        assertFalse(
            harness.overlays.all().any { it.campaignId == "campaign-2" }
        )
    }

    private class Harness {
        val locations = FakeLocationRepository()
        val overlays = FakeLocationOverlayRepository()
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        val updateOverlay = UpdateLocationOverlayUseCase(
            locations,
            overlays,
            context,
            instant,
        )

        suspend fun insertLocation(): Location {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            val location = Location(
                id = "loc-1",
                worldId = "world-1",
                type = LocationType.Place,
                parentLocationId = null,
                name = "Tavern",
                description = "",
                climate = "",
                terrain = "",
                government = "",
                landmarks = emptyList(),
                history = "",
                notes = "World notes",
                createdAt = now,
                updatedAt = now,
            )
            locations.insert(location)
            return location
        }
    }
}

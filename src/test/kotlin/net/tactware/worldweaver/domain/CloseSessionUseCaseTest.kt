package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class CloseSessionUseCaseTest {
    @Test
    fun closeWritesWhatChangedRecap() = runTest {
        val harness = Harness()
        harness.seed()

        val result = harness.closeSession("session-1", "  Keep the gem hidden.  ")

        val closed = assertIs<CloseSessionUseCase.Result.Closed>(result)
        assertTrue(closed.recap.contains("Date: 3 Thaw, 312"))
        assertTrue(closed.recap.contains("Encounter Ambush: Bandits fled."))
        assertTrue(closed.recap.contains("Quest Rescue: 1 complete, 1 open"))
        assertTrue(closed.recap.contains("Party at Riverford"))
        assertTrue(closed.recap.contains("Why it matters next week: Keep the gem hidden."))
        assertEquals(closed.recap, harness.sessions.getById("session-1")!!.recap)
    }

    @Test
    fun closeReturnsNotFoundForUnknownSession() = runTest {
        val harness = Harness()

        val result = harness.closeSession("missing")

        assertIs<CloseSessionUseCase.Result.NotFound>(result)
    }

    private class Harness {
        val sessions = FakeSessionRepository()
        val encounters = FakeEncounterRepository()
        val quests = FakeQuestRepository()
        val overlays = FakeLocationOverlayRepository()
        val locations = FakeLocationRepository()
        val calendars = FakeWorldCalendarRepository()
        val campaigns = FakeCampaignRepository()
        private val instant = InstantProvider { Instant.parse("2026-09-02T12:00:00Z") }
        val closeSession = CloseSessionUseCase(
            sessionRepository = sessions,
            encounterRepository = encounters,
            questRepository = quests,
            locationOverlayRepository = overlays,
            locationRepository = locations,
            worldCalendarRepository = calendars,
            campaignRepository = campaigns,
            instantProvider = instant,
        )

        suspend fun seed() {
            val now = Instant.parse("2026-09-02T12:00:00Z")
            campaigns.insert(
                Campaign(
                    id = "campaign-1",
                    worldId = "world-1",
                    name = "Accord",
                    description = "",
                    notes = "",
                    gameSystem = GameSystem.FifthEdition,
                    status = CampaignStatus.Active,
                    createdAt = now,
                    updatedAt = now,
                )
            )
            calendars.insert(
                WorldCalendar(
                    id = "cal-1",
                    worldId = "world-1",
                    eraSuffix = "",
                    months = listOf(WorldCalendarMonth(id = "thaw", name = "Thaw", days = 30)),
                    weekdays = emptyList(),
                    currentDate = WorldDate(year = 312, monthId = "thaw", day = 3),
                    createdAt = now,
                    updatedAt = now,
                )
            )
            sessions.insert(
                Session(
                    id = "session-1",
                    campaignId = "campaign-1",
                    name = "Tonight",
                    notes = "Prep notes.",
                    inWorldDate = WorldDate(year = 312, monthId = "thaw", day = 3),
                    recap = "",
                    scenes = emptyList(),
                    marchOrder = emptyList(),
                    createdAt = now,
                    updatedAt = now,
                )
            )
            encounters.insert(
                Encounter(
                    id = "enc-1",
                    campaignId = "campaign-1",
                    name = "Ambush",
                    locationId = null,
                    difficulty = EncounterDifficulty.Medium,
                    notes = "",
                    outcomeNote = "Bandits fled.",
                    status = EncounterStatus.Ended,
                    currentRound = 3,
                    currentTurnIndex = 0,
                    participants = emptyList(),
                    createdAt = now,
                    updatedAt = now,
                )
            )
            quests.insert(
                Quest(
                    id = "quest-1",
                    campaignId = "campaign-1",
                    title = "Rescue",
                    summary = "",
                    status = QuestStatus.Active,
                    locationId = null,
                    objectives = listOf(
                        QuestObjective(id = "obj-1", title = "Ask", status = QuestObjectiveStatus.Complete),
                        QuestObjective(id = "obj-2", title = "Find", status = QuestObjectiveStatus.Open),
                    ),
                    links = emptyList(),
                    createdAt = now,
                    updatedAt = now,
                )
            )
            locations.insert(
                Location(
                    id = "loc-1",
                    worldId = "world-1",
                    type = LocationType.Place,
                    parentLocationId = null,
                    name = "Riverford",
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
            )
            overlays.upsert(
                LocationOverlay(
                    campaignId = "campaign-1",
                    locationId = "loc-1",
                    hasPartyPresence = true,
                    notes = "",
                    updatedAt = now,
                )
            )
        }
    }
}

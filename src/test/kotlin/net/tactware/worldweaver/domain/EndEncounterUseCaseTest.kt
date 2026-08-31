package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class EndEncounterUseCaseTest {
    @Test
    fun endStoresOutcomeOnEncounterWhenNoSessionExists() = runTest {
        val harness = Harness()
        harness.insertEncounter("enc-1")

        val result = harness.endEncounter("enc-1", "  Bandits scurry.  ")

        assertIs<EndEncounterUseCase.Result.Ended>(result)
        val ended = harness.encounters.getById("enc-1")!!
        assertEquals(EncounterStatus.Ended, ended.status)
        assertEquals("Bandits scurry.", ended.outcomeNote)
        assertTrue(harness.sessions.all().isEmpty())
    }

    @Test
    fun endAppendsOutcomeToMostRecentlyUpdatedSession() = runTest {
        val harness = Harness()
        harness.insertEncounter("enc-1")
        harness.insertSession(
            id = "old",
            notes = "Earlier.",
            updatedAt = Instant.parse("2026-08-28T12:00:00Z"),
        )
        harness.insertSession(
            id = "current",
            notes = "Tonight.",
            updatedAt = Instant.parse("2026-08-29T10:00:00Z"),
        )

        harness.endEncounter("enc-1", "Victory.")

        val current = harness.sessions.getById("current")!!
        assertEquals("Tonight.\n\nEncounter: Ambush\nVictory.", current.notes)
        assertEquals("Earlier.", harness.sessions.getById("old")!!.notes)
        assertEquals("Victory.", harness.encounters.getById("enc-1")!!.outcomeNote)
    }

    private class Harness {
        val encounters = FakeEncounterRepository()
        val sessions = FakeSessionRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        val endEncounter = EndEncounterUseCase(encounters, sessions, instant)

        suspend fun insertEncounter(id: String) {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            encounters.insert(
                Encounter(
                    id = id,
                    campaignId = "campaign-1",
                    name = "Ambush",
                    locationId = null,
                    difficulty = EncounterDifficulty.Easy,
                    notes = "",
                    outcomeNote = "",
                    status = EncounterStatus.Active,
                    currentRound = 2,
                    currentTurnIndex = 1,
                    participants = emptyList(),
                    createdAt = now,
                    updatedAt = now,
                )
            )
        }

        suspend fun insertSession(
            id: String,
            notes: String,
            updatedAt: Instant,
        ) {
            sessions.insert(
                Session(
                    id = id,
                    campaignId = "campaign-1",
                    name = id,
                    notes = notes,
                    scenes = emptyList(),
                    marchOrder = emptyList(),
                    createdAt = Instant.parse("2026-08-01T12:00:00Z"),
                    updatedAt = updatedAt,
                )
            )
        }
    }
}

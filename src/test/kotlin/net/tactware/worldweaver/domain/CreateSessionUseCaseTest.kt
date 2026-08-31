package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class CreateSessionUseCaseTest {
    @Test
    fun createRequiresActiveCampaign() = runTest {
        val harness = Harness()

        val result = harness.createSession(harness.draft())

        assertIs<CreateSessionUseCase.Result.NoActiveCampaign>(result)
        assertTrue(harness.sessions.all().isEmpty())
    }

    @Test
    fun createRejectsBlankName() = runTest {
        val harness = Harness()
        harness.context.setActiveCampaignId("campaign-1")

        val result = harness.createSession(harness.draft(name = " "))

        assertIs<CreateSessionUseCase.Result.InvalidName>(result)
    }

    @Test
    fun createPersistsNotesScenesAndMarchOrder() = runTest {
        val harness = Harness()
        harness.context.setActiveCampaignId("campaign-1")

        val result = harness.createSession(
            harness.draft(
                name = "Session 1",
                notes = "Recap the tavern.",
                scenes = listOf(
                    SessionScene(id = "", title = "Arrival", notes = "At dusk"),
                    SessionScene(id = "", title = "  ", notes = ""),
                ),
                marchOrder = listOf(
                    MarchOrderEntry(
                        id = "",
                        person = PersonRef.Campaign("pc-1"),
                        displayName = "Bram",
                    )
                ),
            )
        )

        val created = assertIs<CreateSessionUseCase.Result.Created>(result)
        assertEquals("campaign-1", created.session.campaignId)
        assertEquals("Session 1", created.session.name)
        assertEquals("Recap the tavern.", created.session.notes)
        assertEquals(1, created.session.scenes.size)
        assertEquals("Arrival", created.session.scenes[0].title)
        assertEquals("Bram", created.session.marchOrder.single().displayName)
        assertEquals(PersonRef.Campaign("pc-1"), created.session.marchOrder.single().person)
    }

    @Test
    fun createPersistsValidInWorldDate() = runTest {
        val harness = Harness()
        harness.seedCalendar()
        harness.context.setActiveCampaignId("campaign-1")

        val result = harness.createSession(
            harness.draft(
                name = "Session 1",
                inWorldDate = WorldDate(year = 1492, monthId = "m-1", day = 12),
            )
        )

        val created = assertIs<CreateSessionUseCase.Result.Created>(result)
        assertEquals(WorldDate(year = 1492, monthId = "m-1", day = 12), created.session.inWorldDate)
    }

    @Test
    fun createRejectsInvalidInWorldDate() = runTest {
        val harness = Harness()
        harness.seedCalendar()
        harness.context.setActiveCampaignId("campaign-1")

        val result = harness.createSession(
            harness.draft(
                name = "Session 1",
                inWorldDate = WorldDate(year = 1492, monthId = "m-1", day = 40),
            )
        )

        assertIs<CreateSessionUseCase.Result.InvalidDate>(result)
    }

    private class Harness {
        val sessions = FakeSessionRepository()
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        private var nextId = 0
        private val ids = EntityIdFactory { "session-${++nextId}" }
        val campaigns = FakeCampaignRepository()
        val calendars = FakeWorldCalendarRepository()
        val createSession = CreateSessionUseCase(sessions, campaigns, calendars, context, ids, instant)

        fun draft(
            name: String = "Session",
            notes: String = "",
            inWorldDate: WorldDate? = null,
            scenes: List<SessionScene> = emptyList(),
            marchOrder: List<MarchOrderEntry> = emptyList(),
        ): SessionDraft {
            return SessionDraft(
                name = name,
                notes = notes,
                inWorldDate = inWorldDate,
                scenes = scenes,
                marchOrder = marchOrder,
            )
        }

        suspend fun seedCalendar() {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            campaigns.insert(
                Campaign(
                    id = "campaign-1",
                    worldId = "world-1",
                    name = "Heist",
                    description = "",
                    notes = "",
                    gameSystem = null,
                    status = CampaignStatus.Active,
                    createdAt = now,
                    updatedAt = now,
                )
            )
            calendars.insert(
                WorldCalendar(
                    id = "cal-1",
                    worldId = "world-1",
                    eraSuffix = "DR",
                    months = listOf(WorldCalendarMonth(id = "m-1", name = "Hammer", days = 30)),
                    weekdays = emptyList(),
                    currentDate = null,
                    createdAt = now,
                    updatedAt = now,
                )
            )
        }
    }
}

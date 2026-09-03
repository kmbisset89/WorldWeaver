package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class UpdateWorldCalendarUseCaseTest {
    @Test
    fun updatePersistsTrimmedMonthsAndEra() = runTest {
        val harness = Harness()
        val calendar = harness.insertCalendar()

        val result = harness.updateCalendar(
            calendar.id,
            WorldCalendarDraft(
                eraSuffix = "  DR  ",
                months = listOf(
                    WorldCalendarMonth(id = "m-1", name = " Hammer ", days = 30),
                    WorldCalendarMonth(id = "", name = "Alturiak", days = 28),
                ),
                weekdays = listOf(WorldCalendarWeekday(id = "w-1", name = " Moonday ")),
                currentDate = WorldDate(year = 1492, monthId = "m-1", day = 12),
            ),
        )

        assertIs<UpdateWorldCalendarUseCase.Result.Updated>(result)
        val updated = harness.calendars.getById(calendar.id)!!
        assertEquals("DR", updated.eraSuffix)
        assertEquals(listOf("Hammer", "Alturiak"), updated.months.map { it.name })
        assertEquals("Moonday", updated.weekdays.single().name)
        assertEquals(1492, updated.currentDate?.year)
    }

    @Test
    fun updateRejectsDroppingAMonthUsedByASession() = runTest {
        val harness = Harness()
        val calendar = harness.insertCalendar()
        harness.campaigns.insert(
            Campaign(
                id = "camp-1",
                worldId = "world-1",
                name = "Heist",
                description = "",
                notes = "",
                gameSystem = null,
                status = CampaignStatus.Active,
                createdAt = Instant.parse("2026-08-31T12:00:00Z"),
                updatedAt = Instant.parse("2026-08-31T12:00:00Z"),
            )
        )
        harness.sessions.insert(
            Session(
                id = "sess-1",
                campaignId = "camp-1",
                name = "Session 1",
                notes = "",
                inWorldDate = WorldDate(year = 1492, monthId = "m-1", day = 1),
                scenes = emptyList(),
                marchOrder = emptyList(),
                createdAt = Instant.parse("2026-08-31T12:00:00Z"),
                updatedAt = Instant.parse("2026-08-31T12:00:00Z"),
            )
        )

        val result = harness.updateCalendar(
            calendar.id,
            WorldCalendarDraft(
                eraSuffix = "",
                months = listOf(WorldCalendarMonth(id = "m-2", name = "Alturiak", days = 30)),
                weekdays = emptyList(),
                currentDate = null,
            ),
        )

        assertIs<UpdateWorldCalendarUseCase.Result.MonthReferenced>(result)
        assertEquals(listOf("m-1", "m-2"), harness.calendars.getById(calendar.id)?.months?.map { it.id })
    }

    @Test
    fun updateRejectsInvalidCurrentDate() = runTest {
        val harness = Harness()
        val calendar = harness.insertCalendar()

        val result = harness.updateCalendar(
            calendar.id,
            WorldCalendarDraft(
                eraSuffix = "",
                months = calendar.months,
                weekdays = calendar.weekdays,
                currentDate = WorldDate(year = 1, monthId = "m-1", day = 40),
            ),
        )

        assertIs<UpdateWorldCalendarUseCase.Result.InvalidCurrentDate>(result)
    }

    private class Harness {
        val calendars = FakeWorldCalendarRepository()
        val campaigns = FakeCampaignRepository()
        val sessions = FakeSessionRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-31T13:00:00Z") }
        private var nextId = 0
        private val ids = EntityIdFactory { "cal-update-${++nextId}" }
        val updateCalendar = UpdateWorldCalendarUseCase(
            calendars,
            FindSessionCalendarMonthIdsForWorldUseCase(campaigns, sessions),
            ids,
            instant,
        )

        suspend fun insertCalendar(): WorldCalendar {
            val now = Instant.parse("2026-08-31T12:00:00Z")
            val calendar = WorldCalendar(
                id = "cal-1",
                worldId = "world-1",
                eraSuffix = "",
                months = listOf(
                    WorldCalendarMonth(id = "m-1", name = "Hammer", days = 30),
                    WorldCalendarMonth(id = "m-2", name = "Alturiak", days = 30),
                ),
                weekdays = emptyList(),
                currentDate = null,
                createdAt = now,
                updatedAt = now,
            )
            calendars.insert(calendar)
            return calendar
        }
    }
}

package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class UpdateWorldCalendarObservanceUseCaseTest {
    @Test
    fun updateRejectsUnknownObservance() = runTest {
        val harness = Harness()
        harness.insertCalendar()

        val result = harness.update("missing", draft())

        assertIs<UpdateWorldCalendarObservanceUseCase.Result.NotFound>(result)
    }

    @Test
    fun updateRejectsDuplicateName() = runTest {
        val harness = Harness()
        harness.insertCalendar()
        harness.insertObservance(id = "obs-1", name = "Midwinter")
        harness.insertObservance(id = "obs-2", name = "Harvestide")

        val result = harness.update("obs-2", draft(name = "midwinter"))

        assertIs<UpdateWorldCalendarObservanceUseCase.Result.DuplicateName>(result)
        assertEquals("Harvestide", harness.observances.getById("obs-2")?.name)
    }

    @Test
    fun updatePersistsTrimmedFieldsAndValidLore() = runTest {
        val harness = Harness()
        harness.insertCalendar()
        harness.insertObservance(id = "obs-1", name = "Midwinter")
        harness.lore.insert(harness.loreEntry(id = "lore-1", worldId = "world-1"))
        harness.lore.insert(harness.loreEntry(id = "lore-2", worldId = "other"))

        val result = harness.update(
            "obs-1",
            draft(
                name = "  Highharvestide  ",
                notes = " Feast ",
                monthId = "m-1",
                day = 1,
                year = 1492,
                loreIds = listOf("lore-1", "lore-2"),
            ),
        )

        assertIs<UpdateWorldCalendarObservanceUseCase.Result.Updated>(result)
        val updated = harness.observances.getById("obs-1")!!
        assertEquals("Highharvestide", updated.name)
        assertEquals("Feast", updated.notes)
        assertEquals(1, updated.day)
        assertEquals(1492, updated.year)
        assertEquals(listOf("lore-1"), updated.loreIds)
    }

    private fun draft(
        name: String = "Midwinter",
        notes: String = "",
        monthId: String = "m-1",
        day: Int = 12,
        year: Int? = null,
        loreIds: List<String> = emptyList(),
    ): WorldCalendarObservanceDraft {
        return WorldCalendarObservanceDraft(
            name = name,
            notes = notes,
            kind = WorldCalendarObservanceKind.ImportantDay,
            monthId = monthId,
            day = day,
            year = year,
            loreIds = loreIds,
        )
    }

    private class Harness {
        val observances = FakeWorldCalendarObservanceRepository()
        val calendars = FakeWorldCalendarRepository()
        val lore = FakeLoreRepository()
        private val instant = InstantProvider { Instant.parse("2026-09-04T13:00:00Z") }
        private val updateObservance = UpdateWorldCalendarObservanceUseCase(
            observances,
            calendars,
            lore,
            instant,
        )

        suspend fun update(id: String, draft: WorldCalendarObservanceDraft) = updateObservance(id, draft)

        suspend fun insertCalendar() {
            val now = Instant.parse("2026-09-04T12:00:00Z")
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

        suspend fun insertObservance(id: String, name: String) {
            val now = Instant.parse("2026-09-04T12:00:00Z")
            observances.insert(
                WorldCalendarObservance(
                    id = id,
                    worldId = "world-1",
                    name = name,
                    notes = "",
                    kind = WorldCalendarObservanceKind.Holiday,
                    monthId = "m-1",
                    day = 12,
                    year = null,
                    loreIds = emptyList(),
                    createdAt = now,
                    updatedAt = now,
                )
            )
        }

        fun loreEntry(id: String, worldId: String): Lore {
            val now = Instant.parse("2026-09-04T12:00:00Z")
            return Lore(
                id = id,
                worldId = worldId,
                title = "Entry",
                content = "Body",
                category = LoreCategory.History,
                tags = emptyList(),
                relatedEntryIds = emptyList(),
                secrets = emptyList(),
                locationId = null,
                characterId = null,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}

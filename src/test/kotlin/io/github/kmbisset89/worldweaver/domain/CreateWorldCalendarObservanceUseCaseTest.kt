package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class CreateWorldCalendarObservanceUseCaseTest {
    @Test
    fun createRequiresActiveWorld() = runTest {
        val harness = Harness()

        val result = harness.create(draft())

        assertIs<CreateWorldCalendarObservanceUseCase.Result.NoActiveWorld>(result)
        assertTrue(harness.observances.all().isEmpty())
    }

    @Test
    fun createRejectsBlankName() = runTest {
        val harness = Harness()
        harness.readyWorld()

        val result = harness.create(draft(name = "  "))

        assertIs<CreateWorldCalendarObservanceUseCase.Result.InvalidName>(result)
    }

    @Test
    fun createRejectsDuplicateName() = runTest {
        val harness = Harness()
        harness.readyWorld()
        harness.create(draft(name = "Midwinter"))

        val result = harness.create(draft(name = "midwinter"))

        assertIs<CreateWorldCalendarObservanceUseCase.Result.DuplicateName>(result)
        assertEquals(1, harness.observances.all().size)
    }

    @Test
    fun createRejectsDayOutsideMonth() = runTest {
        val harness = Harness()
        harness.readyWorld()

        val result = harness.create(draft(day = 40))

        assertIs<CreateWorldCalendarObservanceUseCase.Result.InvalidDate>(result)
    }

    @Test
    fun createDropsLoreFromAnotherWorld() = runTest {
        val harness = Harness()
        harness.readyWorld()
        harness.lore.insert(lore(id = "lore-1", worldId = "world-1", title = "The Sundering"))
        harness.lore.insert(lore(id = "lore-2", worldId = "world-2", title = "Other"))

        val result = harness.create(draft(loreIds = listOf("lore-1", "lore-2", "missing")))

        val created = assertIs<CreateWorldCalendarObservanceUseCase.Result.Created>(result)
        assertEquals(listOf("lore-1"), created.observance.loreIds)
        assertEquals("Midwinter", created.observance.name)
        assertNull(created.observance.year)
        assertTrue(created.observance.matches(WorldDate(year = 847, monthId = "m-1", day = 12)))
        assertTrue(created.observance.matches(WorldDate(year = 1, monthId = "m-1", day = 12)))
        assertTrue(!created.observance.matches(WorldDate(year = 847, monthId = "m-1", day = 13)))
    }

    @Test
    fun datedObservanceMatchesOnlyThatYear() = runTest {
        val harness = Harness()
        harness.readyWorld()

        val result = harness.create(draft(name = "Broken Swords", year = 1489, day = 3))

        val created = assertIs<CreateWorldCalendarObservanceUseCase.Result.Created>(result)
        assertEquals(1489, created.observance.year)
        assertTrue(created.observance.matches(WorldDate(year = 1489, monthId = "m-1", day = 3)))
        assertTrue(!created.observance.matches(WorldDate(year = 1490, monthId = "m-1", day = 3)))
    }

    private fun draft(
        name: String = "Midwinter",
        monthId: String = "m-1",
        day: Int = 12,
        year: Int? = null,
        loreIds: List<String> = emptyList(),
    ): WorldCalendarObservanceDraft {
        return WorldCalendarObservanceDraft(
            name = name,
            notes = " Festival ",
            kind = WorldCalendarObservanceKind.Holiday,
            monthId = monthId,
            day = day,
            year = year,
            loreIds = loreIds,
        )
    }

    private fun lore(id: String, worldId: String, title: String): Lore {
        val now = Instant.parse("2026-09-04T12:00:00Z")
        return Lore(
            id = id,
            worldId = worldId,
            title = title,
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

    private class Harness {
        val observances = FakeWorldCalendarObservanceRepository()
        val calendars = FakeWorldCalendarRepository()
        val lore = FakeLoreRepository()
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-09-04T12:00:00Z") }
        private var nextId = 0
        private val ids = EntityIdFactory { "obs-${++nextId}" }
        private val createObservance = CreateWorldCalendarObservanceUseCase(
            observances,
            calendars,
            lore,
            context,
            ids,
            instant,
        )

        suspend fun create(draft: WorldCalendarObservanceDraft) = createObservance(draft)

        suspend fun readyWorld() {
            context.setActiveWorldId("world-1")
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
    }
}

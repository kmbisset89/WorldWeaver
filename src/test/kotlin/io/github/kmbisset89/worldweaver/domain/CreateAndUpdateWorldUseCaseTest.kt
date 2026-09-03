package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class CreateAndUpdateWorldUseCaseTest {
    @Test
    fun createRejectsBlankName() = runTest {
        val harness = Harness()

        val result = harness.createWorld("   ", "A setting")

        assertIs<CreateWorldUseCase.Result.InvalidName>(result)
        assertTrue(harness.worlds.all().isEmpty())
    }

    @Test
    fun createPersistsWorldAndSetsItActive() = runTest {
        val harness = Harness()

        val result = harness.createWorld("Faerun", "Forgotten Realms")

        val created = assertIs<CreateWorldUseCase.Result.Created>(result)
        assertEquals("Faerun", created.world.name)
        assertEquals(GameSystem.FifthEdition, created.world.defaultGameSystem)
        assertEquals(created.world.id, harness.context.get().activeWorldId)
        assertEquals(1, harness.worlds.all().size)
        val calendar = harness.calendars.getByWorld(created.world.id)
        assertEquals(12, calendar?.months?.size)
        assertEquals(7, calendar?.weekdays?.size)
    }

    @Test
    fun updateRejectsEmptyName() = runTest {
        val harness = Harness()
        val created = assertIs<CreateWorldUseCase.Result.Created>(
            harness.createWorld("Faerun", "")
        )

        val result = harness.updateWorld(
            created.world.id,
            "  ",
            "updated",
            GameSystem.FifthEdition,
        )

        assertIs<UpdateWorldUseCase.Result.InvalidName>(result)
        assertEquals("Faerun", harness.worlds.getById(created.world.id)?.name)
    }

    @Test
    fun updatePersistsNameAndDescription() = runTest {
        val harness = Harness()
        val created = assertIs<CreateWorldUseCase.Result.Created>(
            harness.createWorld("Faerun", "")
        )

        val result = harness.updateWorld(
            created.world.id,
            "Toril",
            "The world",
            GameSystem.Pathfinder2E,
        )

        assertIs<UpdateWorldUseCase.Result.Updated>(result)
        val updated = harness.worlds.getById(created.world.id)
        assertEquals("Toril", updated?.name)
        assertEquals("The world", updated?.description)
        assertEquals(GameSystem.Pathfinder2E, updated?.defaultGameSystem)
    }

    private class Harness {
        val worlds = FakeWorldRepository()
        val calendars = FakeWorldCalendarRepository()
        val campaigns = FakeCampaignRepository()
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        private var nextId = 0
        private val ids = EntityIdFactory { "world-${++nextId}" }
        private val setActiveWorld = SetActiveWorldUseCase(worlds, campaigns, context, instant)
        val createWorld = CreateWorldUseCase(
            worlds,
            calendars,
            DefaultWorldCalendarFactory(ids),
            ids,
            instant,
            setActiveWorld,
        )
        val updateWorld = UpdateWorldUseCase(worlds, instant)
    }
}

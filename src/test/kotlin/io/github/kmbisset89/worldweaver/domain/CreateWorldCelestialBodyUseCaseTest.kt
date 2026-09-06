package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class CreateWorldCelestialBodyUseCaseTest {
    @Test
    fun createRequiresActiveWorld() = runTest {
        val harness = Harness()

        val result = harness.create(draft())

        assertIs<CreateWorldCelestialBodyUseCase.Result.NoActiveWorld>(result)
        assertTrue(harness.bodies.all().isEmpty())
    }

    @Test
    fun createRejectsBlankName() = runTest {
        val harness = Harness()
        harness.readyWorld()

        val result = harness.create(draft(name = "  "))

        assertIs<CreateWorldCelestialBodyUseCase.Result.InvalidName>(result)
    }

    @Test
    fun createRejectsDuplicateName() = runTest {
        val harness = Harness()
        harness.readyWorld()
        harness.create(draft(name = "The Moon"))

        val result = harness.create(draft(name = "the moon"))

        assertIs<CreateWorldCelestialBodyUseCase.Result.DuplicateName>(result)
        assertEquals(1, harness.bodies.all().size)
    }

    @Test
    fun createRejectsInvalidPeriod() = runTest {
        val harness = Harness()
        harness.readyWorld()

        val result = harness.create(draft(periodDays = 0))

        assertIs<CreateWorldCelestialBodyUseCase.Result.InvalidPeriod>(result)
    }

    @Test
    fun createAppendsAndTrimsFields() = runTest {
        val harness = Harness()
        harness.readyWorld()
        harness.create(draft(name = "The Sun", periodDays = 360))

        val result = harness.create(
            draft(
                name = "  The Moon  ",
                notes = " Silver  ",
                kind = CelestialBodyKind.Moon,
                periodDays = 29,
                epochOffsetDays = 4,
            )
        )

        val created = assertIs<CreateWorldCelestialBodyUseCase.Result.Created>(result)
        assertEquals("The Moon", created.body.name)
        assertEquals("Silver", created.body.notes)
        assertEquals(CelestialBodyKind.Moon, created.body.kind)
        assertEquals(29, created.body.periodDays)
        assertEquals(4, created.body.epochOffsetDays)
        assertEquals(1, created.body.sortIndex)
        assertEquals("world-1", created.body.worldId)
    }

    private fun draft(
        name: String = "The Moon",
        notes: String = "",
        kind: CelestialBodyKind = CelestialBodyKind.Moon,
        periodDays: Int = 29,
        epochOffsetDays: Int = 0,
    ): WorldCelestialBodyDraft {
        return WorldCelestialBodyDraft(
            name = name,
            notes = notes,
            kind = kind,
            periodDays = periodDays,
            epochOffsetDays = epochOffsetDays,
        )
    }

    private class Harness {
        val bodies = FakeWorldCelestialBodyRepository()
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-09-05T12:00:00Z") }
        private var nextId = 0
        private val ids = EntityIdFactory { "body-${++nextId}" }
        private val createBody = CreateWorldCelestialBodyUseCase(
            bodies,
            context,
            ids,
            instant,
        )

        suspend fun create(draft: WorldCelestialBodyDraft) = createBody(draft)

        fun readyWorld() {
            context.setActiveWorldId("world-1")
        }
    }
}

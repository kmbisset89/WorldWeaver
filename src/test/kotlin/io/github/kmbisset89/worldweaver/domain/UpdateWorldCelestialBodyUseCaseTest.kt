package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class UpdateWorldCelestialBodyUseCaseTest {
    @Test
    fun updateRejectsUnknownBody() = runTest {
        val harness = Harness()

        val result = harness.update("missing", draft())

        assertIs<UpdateWorldCelestialBodyUseCase.Result.NotFound>(result)
    }

    @Test
    fun updateRejectsDuplicateName() = runTest {
        val harness = Harness()
        harness.insert(id = "body-1", name = "The Sun")
        harness.insert(id = "body-2", name = "The Moon")

        val result = harness.update("body-2", draft(name = "the sun"))

        assertIs<UpdateWorldCelestialBodyUseCase.Result.DuplicateName>(result)
        assertEquals("The Moon", harness.bodies.getById("body-2")?.name)
    }

    @Test
    fun updateRejectsInvalidPeriod() = runTest {
        val harness = Harness()
        harness.insert(id = "body-1", name = "The Moon")

        val result = harness.update("body-1", draft(periodDays = 0))

        assertIs<UpdateWorldCelestialBodyUseCase.Result.InvalidPeriod>(result)
    }

    @Test
    fun updatePersistsTrimmedFieldsAndSortIndex() = runTest {
        val harness = Harness()
        harness.insert(id = "body-1", name = "The Moon")

        val result = harness.update(
            "body-1",
            draft(
                name = "  Selûne  ",
                notes = " Silver  ",
                kind = CelestialBodyKind.Moon,
                periodDays = 30,
                epochOffsetDays = 2,
                sortIndex = 4,
            ),
        )

        assertIs<UpdateWorldCelestialBodyUseCase.Result.Updated>(result)
        val updated = harness.bodies.getById("body-1")!!
        assertEquals("Selûne", updated.name)
        assertEquals("Silver", updated.notes)
        assertEquals(30, updated.periodDays)
        assertEquals(2, updated.epochOffsetDays)
        assertEquals(4, updated.sortIndex)
    }

    private fun draft(
        name: String = "The Moon",
        notes: String = "",
        kind: CelestialBodyKind = CelestialBodyKind.Moon,
        periodDays: Int = 29,
        epochOffsetDays: Int = 0,
        sortIndex: Int? = null,
    ): WorldCelestialBodyDraft {
        return WorldCelestialBodyDraft(
            name = name,
            notes = notes,
            kind = kind,
            periodDays = periodDays,
            epochOffsetDays = epochOffsetDays,
            sortIndex = sortIndex,
        )
    }

    private class Harness {
        val bodies = FakeWorldCelestialBodyRepository()
        private val instant = InstantProvider { Instant.parse("2026-09-05T13:00:00Z") }
        private val updateBody = UpdateWorldCelestialBodyUseCase(bodies, instant)

        suspend fun update(id: String, draft: WorldCelestialBodyDraft) = updateBody(id, draft)

        suspend fun insert(id: String, name: String) {
            val now = Instant.parse("2026-09-05T12:00:00Z")
            bodies.insert(
                WorldCelestialBody(
                    id = id,
                    worldId = "world-1",
                    name = name,
                    notes = "",
                    kind = CelestialBodyKind.Moon,
                    periodDays = 29,
                    epochOffsetDays = 0,
                    sortIndex = 0,
                    createdAt = now,
                    updatedAt = now,
                )
            )
        }
    }
}

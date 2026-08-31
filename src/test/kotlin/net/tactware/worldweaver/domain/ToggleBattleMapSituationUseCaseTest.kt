package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class ToggleBattleMapSituationUseCaseTest {
    @Test
    fun toggleFlipsVisibility() = runTest {
        val harness = Harness()
        harness.situations.insert(harness.sample(visible = true))

        val first = harness.toggleSituation("sit-1")
        val hidden = assertIs<ToggleBattleMapSituationUseCase.Result.Toggled>(first)
        assertEquals(false, hidden.situation.visible)

        val second = harness.toggleSituation("sit-1")
        val shown = assertIs<ToggleBattleMapSituationUseCase.Result.Toggled>(second)
        assertEquals(true, shown.situation.visible)
    }

    @Test
    fun toggleMissingReturnsNotFound() = runTest {
        val harness = Harness()

        val result = harness.toggleSituation("missing")

        assertIs<ToggleBattleMapSituationUseCase.Result.NotFound>(result)
    }

    private class Harness {
        val situations = FakeBattleMapSituationRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-30T12:00:00Z") }
        val toggleSituation = ToggleBattleMapSituationUseCase(situations, instant)

        fun sample(visible: Boolean): BattleMapSituation {
            val now = Instant.parse("2026-08-30T12:00:00Z")
            return BattleMapSituation(
                id = "sit-1",
                battleMapId = "map-1",
                name = "Flood",
                visible = visible,
                sortIndex = 0,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}

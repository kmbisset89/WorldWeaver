package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class PlaceBattleMapItemUseCaseTest {
    @Test
    fun placesNamedItemOnTheMap() = runTest {
        val harness = Harness()
        harness.insertMap()

        val result = harness.place("Rusty sword", GridCell(1, 2))
        val placed = assertIs<PlaceBattleMapItemUseCase.Result.Placed>(result)
        assertEquals("Rusty sword", placed.item.name)
        assertEquals(GridCell(1, 2), placed.item.cell)
        assertEquals(listOf(placed.item), harness.maps.getById("map-1")!!.items)
    }

    @Test
    fun blankNameIsInvalid() = runTest {
        val harness = Harness()
        harness.insertMap()

        assertIs<PlaceBattleMapItemUseCase.Result.InvalidName>(
            harness.place("  ", GridCell(0, 0)),
        )
        assertTrue(harness.maps.getById("map-1")!!.items.isEmpty())
    }

    @Test
    fun cellOffTheMapIsInvalid() = runTest {
        val harness = Harness()
        harness.insertMap()

        assertIs<PlaceBattleMapItemUseCase.Result.InvalidCell>(
            harness.place("Torch", GridCell(9, 0)),
        )
    }

    @Test
    fun missingMapIsNotFound() = runTest {
        val harness = Harness()
        assertIs<PlaceBattleMapItemUseCase.Result.NotFound>(
            harness.place("Torch", GridCell(0, 0)),
        )
    }

    private class Harness {
        val maps = FakeBattleMapRepository()
        private val placeItem = PlaceBattleMapItemUseCase(
            battleMapRepository = maps,
            entityIdFactory = EntityIdFactory { "item-1" },
            instantProvider = InstantProvider { Instant.parse("2026-08-30T18:00:00Z") },
        )

        suspend fun insertMap() {
            maps.insert(
                BattleMap(
                    id = "map-1",
                    campaignId = "campaign-1",
                    name = "Cave",
                    originalWidth = 512,
                    originalHeight = 512,
                    tileSizePx = 256,
                    minZoom = 0,
                    maxZoom = 0,
                    columns = 3,
                    rows = 3,
                    createdAt = Instant.parse("2026-08-30T12:00:00Z"),
                    updatedAt = Instant.parse("2026-08-30T12:00:00Z"),
                ),
            )
        }

        suspend fun place(name: String, cell: GridCell): PlaceBattleMapItemUseCase.Result {
            return placeItem("map-1", name, cell)
        }
    }
}

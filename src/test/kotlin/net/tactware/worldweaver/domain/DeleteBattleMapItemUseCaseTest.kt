package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class DeleteBattleMapItemUseCaseTest {
    @Test
    fun removesTheItem() = runTest {
        val harness = Harness()
        harness.insertMap(
            items = listOf(
                BattleMapItem(id = "item-1", name = "Torch", cell = GridCell(0, 0)),
            ),
        )

        assertIs<DeleteBattleMapItemUseCase.Result.Deleted>(
            harness.delete("item-1"),
        )
        assertTrue(harness.maps.getById("map-1")!!.items.isEmpty())
    }

    @Test
    fun missingItemIsNotFound() = runTest {
        val harness = Harness()
        harness.insertMap()

        assertIs<DeleteBattleMapItemUseCase.Result.NotFound>(
            harness.delete("missing"),
        )
    }

    private class Harness {
        val maps = FakeBattleMapRepository()
        private val deleteItem = DeleteBattleMapItemUseCase(
            battleMapRepository = maps,
            instantProvider = InstantProvider { Instant.parse("2026-08-30T18:00:00Z") },
        )

        suspend fun insertMap(items: List<BattleMapItem> = emptyList()) {
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
                    items = items,
                    createdAt = Instant.parse("2026-08-30T12:00:00Z"),
                    updatedAt = Instant.parse("2026-08-30T12:00:00Z"),
                ),
            )
        }

        suspend fun delete(itemId: String): DeleteBattleMapItemUseCase.Result {
            return deleteItem("map-1", itemId)
        }
    }
}

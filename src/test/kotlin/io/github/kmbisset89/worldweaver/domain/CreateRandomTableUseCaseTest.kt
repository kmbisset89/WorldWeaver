package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class CreateRandomTableUseCaseTest {
    @Test
    fun createRequiresActiveWorld() = runTest {
        val harness = Harness()

        val result = harness.createTable(draft())

        assertIs<CreateRandomTableUseCase.Result.NoActiveWorld>(result)
        assertTrue(harness.tables.all().isEmpty())
    }

    @Test
    fun createRejectsBlankName() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")

        val result = harness.createTable(draft(name = "  "))

        assertIs<CreateRandomTableUseCase.Result.InvalidName>(result)
    }

    @Test
    fun createRejectsRowsWithoutALabel() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")

        val result = harness.createTable(
            draft(
                rows = listOf(RandomTableRowDraft(id = null, label = "  ", weight = 1, nestedTableId = null)),
            )
        )

        assertIs<CreateRandomTableUseCase.Result.InvalidRows>(result)
    }

    @Test
    fun createStoresWorldOwnedTable() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")

        val result = harness.createTable(
            draft(
                name = "  Weather  ",
                notes = " Harbor skies ",
                rows = listOf(
                    RandomTableRowDraft(id = null, label = " Clear ", weight = 2, nestedTableId = null),
                    RandomTableRowDraft(id = null, label = "Storm", weight = 1, nestedTableId = null),
                ),
            )
        )

        val created = assertIs<CreateRandomTableUseCase.Result.Created>(result)
        assertEquals("world-1", created.table.worldId)
        assertEquals("Weather", created.table.name)
        assertEquals("Harbor skies", created.table.notes)
        assertEquals(listOf("Clear", "Storm"), created.table.rows.map { it.label })
        assertEquals(listOf(2, 1), created.table.rows.map { it.weight })
    }

    private fun draft(
        name: String = "Weather",
        notes: String = "",
        rows: List<RandomTableRowDraft> = listOf(
            RandomTableRowDraft(id = null, label = "Clear", weight = 1, nestedTableId = null),
        ),
    ): RandomTableDraft {
        return RandomTableDraft(name = name, notes = notes, rows = rows)
    }

    private class Harness {
        val tables = FakeRandomTableRepository()
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-09-06T12:00:00Z") }
        private var nextId = 0
        private val ids = EntityIdFactory { "tbl-${++nextId}" }
        val createTable = CreateRandomTableUseCase(tables, context, ids, instant)
    }
}

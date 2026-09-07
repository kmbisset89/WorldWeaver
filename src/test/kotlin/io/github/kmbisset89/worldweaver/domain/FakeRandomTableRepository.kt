package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeRandomTableRepository : RandomTableRepository {
    private val tables = MutableStateFlow<List<RandomTable>>(emptyList())

    fun all(): List<RandomTable> = tables.value

    override fun observeByWorld(worldId: String): Flow<List<RandomTable>> {
        return tables.map { list -> list.filter { it.worldId == worldId } }
    }

    override suspend fun getById(id: String): RandomTable? {
        return tables.value.firstOrNull { it.id == id }
    }

    override suspend fun getByWorld(worldId: String): List<RandomTable> {
        return tables.value.filter { it.worldId == worldId }
    }

    override suspend fun search(query: String): List<RandomTable> {
        return tables.value.filter { table ->
            table.name.contains(query, ignoreCase = true) ||
                table.notes.contains(query, ignoreCase = true)
        }
    }

    override suspend fun insert(table: RandomTable) {
        tables.value = tables.value + table
    }

    override suspend fun update(table: RandomTable) {
        tables.value = tables.value.map { current ->
            if (current.id == table.id) table else current
        }
    }

    override suspend fun delete(id: String) {
        tables.value = tables.value.filterNot { it.id == id }
    }
}

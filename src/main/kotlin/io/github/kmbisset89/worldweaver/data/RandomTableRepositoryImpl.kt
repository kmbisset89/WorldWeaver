package io.github.kmbisset89.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import io.github.kmbisset89.worldweaver.domain.RandomTable
import io.github.kmbisset89.worldweaver.domain.RandomTableRepository

internal class RandomTableRepositoryImpl(
    private val tableDao: RandomTableDao,
    private val rowDao: RandomTableRowDao,
    private val converter: RandomTableEntityConverter,
) : RandomTableRepository {
    override fun observeByWorld(worldId: String): Flow<List<RandomTable>> {
        return combine(
            tableDao.observeByWorld(worldId),
            rowDao.observeByWorld(worldId),
        ) { tables, rows ->
            assemble(tables, rows)
        }
    }

    override suspend fun getById(id: String): RandomTable? {
        val entity = tableDao.getById(id) ?: return null
        return converter.toTable(entity, rowDao.getByTable(id))
    }

    override suspend fun getByWorld(worldId: String): List<RandomTable> {
        return assemble(
            tableDao.getByWorld(worldId),
            rowDao.getByWorld(worldId),
        )
    }

    override suspend fun search(query: String): List<RandomTable> {
        return tableDao.searchLike(query).map { entity ->
            converter.toTable(entity, emptyList())
        }
    }

    override suspend fun insert(table: RandomTable) {
        tableDao.insert(converter.toEntity(table))
        replaceRows(table)
    }

    override suspend fun update(table: RandomTable) {
        tableDao.update(converter.toEntity(table))
        replaceRows(table)
    }

    override suspend fun delete(id: String) {
        tableDao.delete(id)
    }

    private suspend fun replaceRows(table: RandomTable) {
        rowDao.deleteByTable(table.id)
        val rows = converter.toRowEntities(table)
        if (rows.isNotEmpty()) {
            rowDao.insertAll(rows)
        }
    }

    private fun assemble(
        tables: List<RandomTableEntity>,
        rows: List<RandomTableRowEntity>,
    ): List<RandomTable> {
        val byTable = rows.groupBy { it.tableId }
        return tables.map { entity ->
            converter.toTable(entity, byTable[entity.id].orEmpty())
        }
    }
}

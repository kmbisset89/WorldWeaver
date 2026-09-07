package io.github.kmbisset89.worldweaver.data

import io.github.kmbisset89.worldweaver.domain.RandomTable
import io.github.kmbisset89.worldweaver.domain.RandomTableRow
import java.time.Instant

internal class RandomTableEntityConverter {
    fun toTable(
        entity: RandomTableEntity,
        rowEntities: List<RandomTableRowEntity>,
    ): RandomTable {
        return RandomTable(
            id = entity.id,
            worldId = entity.worldId,
            name = entity.name,
            notes = entity.notes,
            rows = rowEntities.sortedBy { it.sortIndex }.map(::toRow),
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMillis),
            updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis),
        )
    }

    fun toEntity(table: RandomTable): RandomTableEntity {
        return RandomTableEntity(
            id = table.id,
            worldId = table.worldId,
            name = table.name,
            notes = table.notes,
            createdAtEpochMillis = table.createdAt.toEpochMilli(),
            updatedAtEpochMillis = table.updatedAt.toEpochMilli(),
        )
    }

    fun toRowEntities(table: RandomTable): List<RandomTableRowEntity> {
        return table.rows.mapIndexed { index, row ->
            RandomTableRowEntity(
                id = row.id,
                tableId = table.id,
                sortIndex = index,
                label = row.label,
                weight = row.weight,
                nestedTableId = row.nestedTableId,
            )
        }
    }

    private fun toRow(entity: RandomTableRowEntity): RandomTableRow {
        return RandomTableRow(
            id = entity.id,
            label = entity.label,
            weight = entity.weight,
            nestedTableId = entity.nestedTableId,
        )
    }
}

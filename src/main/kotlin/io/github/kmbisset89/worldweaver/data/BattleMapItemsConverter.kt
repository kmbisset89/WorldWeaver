package io.github.kmbisset89.worldweaver.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import io.github.kmbisset89.worldweaver.domain.BattleMapItem
import io.github.kmbisset89.worldweaver.domain.GridCell

internal class BattleMapItemsConverter(
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    fun encode(items: List<BattleMapItem>): String {
        if (items.isEmpty()) {
            return ""
        }
        return json.encodeToString(
            ListSerializer(StoredItem.serializer()),
            items.map(StoredItem::from),
        )
    }

    fun decode(raw: String): List<BattleMapItem> {
        if (raw.isBlank()) {
            return emptyList()
        }
        return json.decodeFromString(ListSerializer(StoredItem.serializer()), raw)
            .map { stored -> stored.toItem() }
    }

    @Serializable
    private data class StoredItem(
        val id: String,
        val name: String,
        val column: Int,
        val row: Int,
    ) {
        fun toItem(): BattleMapItem {
            return BattleMapItem(
                id = id,
                name = name,
                cell = GridCell(column = column, row = row),
            )
        }

        companion object {
            fun from(item: BattleMapItem): StoredItem {
                return StoredItem(
                    id = item.id,
                    name = item.name,
                    column = item.cell.column,
                    row = item.cell.row,
                )
            }
        }
    }
}

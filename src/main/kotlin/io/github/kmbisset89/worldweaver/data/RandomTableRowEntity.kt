package io.github.kmbisset89.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "random_table_rows",
    foreignKeys = [
        ForeignKey(
            entity = RandomTableEntity::class,
            parentColumns = ["id"],
            childColumns = ["tableId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = RandomTableEntity::class,
            parentColumns = ["id"],
            childColumns = ["nestedTableId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("tableId"),
        Index("nestedTableId"),
    ],
)
internal data class RandomTableRowEntity(
    @PrimaryKey val id: String,
    val tableId: String,
    val sortIndex: Int,
    val label: String,
    val weight: Int,
    val nestedTableId: String?,
)

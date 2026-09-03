package io.github.kmbisset89.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lore",
    foreignKeys = [
        ForeignKey(
            entity = WorldEntity::class,
            parentColumns = ["id"],
            childColumns = ["worldId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = LocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("worldId"),
        Index("locationId"),
        Index("category"),
    ],
)
internal data class LoreEntity(
    @PrimaryKey val id: String,
    val worldId: String,
    val title: String,
    val content: String,
    val category: String,
    val tags: String,
    val relatedEntryIds: String,
    val locationId: String?,
    val characterId: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

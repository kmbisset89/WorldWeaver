package io.github.kmbisset89.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "world_maps",
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
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("worldId"),
        Index(value = ["locationId"], unique = true),
    ],
)
internal data class WorldMapEntity(
    @PrimaryKey val id: String,
    val worldId: String,
    val locationId: String?,
    val originalWidth: Int,
    val originalHeight: Int,
    val tileSizePx: Int,
    val minZoom: Int,
    val maxZoom: Int,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

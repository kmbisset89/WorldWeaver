package net.tactware.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "locations",
    foreignKeys = [
        ForeignKey(
            entity = WorldEntity::class,
            parentColumns = ["id"],
            childColumns = ["worldId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("worldId"),
        Index("parentLocationId"),
    ],
)
internal data class LocationEntity(
    @PrimaryKey val id: String,
    val worldId: String,
    val type: String,
    val parentLocationId: String?,
    val name: String,
    val description: String,
    val climate: String,
    val terrain: String,
    val government: String,
    val landmarks: String,
    val history: String,
    val notes: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

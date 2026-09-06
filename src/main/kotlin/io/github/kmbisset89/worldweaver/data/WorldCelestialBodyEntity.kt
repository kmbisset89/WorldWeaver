package io.github.kmbisset89.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "world_celestial_bodies",
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
        Index("sortIndex"),
    ],
)
internal data class WorldCelestialBodyEntity(
    @PrimaryKey val id: String,
    val worldId: String,
    val name: String,
    val notes: String,
    val kind: String,
    val periodDays: Int,
    val epochOffsetDays: Int,
    val sortIndex: Int,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

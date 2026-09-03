package io.github.kmbisset89.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "factions",
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
    ],
)
internal data class FactionEntity(
    @PrimaryKey val id: String,
    val worldId: String,
    val name: String,
    val description: String,
    val goals: String,
    val notes: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

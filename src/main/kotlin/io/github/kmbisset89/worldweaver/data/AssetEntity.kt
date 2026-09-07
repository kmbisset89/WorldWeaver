package io.github.kmbisset89.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "assets",
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
internal data class AssetEntity(
    @PrimaryKey val id: String,
    val worldId: String,
    val displayName: String,
    val originalFileName: String,
    val notes: String,
    val byteSize: Long,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
